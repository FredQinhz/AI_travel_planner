/**
 * 高德地图服务类
 * 封装高德地图API，提供位置标记、路线绘制等通用功能
 */

export interface Location {
  name: string;
  lat: number;
  lng: number;
  type: string;
  description?: string;
}

export interface Route {
  start: Location;
  end: Location;
  waypoints?: Location[];
}

export type RouteType = 'driving' | 'transit' | 'walking';

export class MapService {
  private map: any = null;
  private markers: any[] = [];
  private polylines: any[] = [];
  private infoWindows: any[] = [];
  private currentHighlightedMarker: any = null;
  private drivingService: any = null;
  private transitService: any = null;
  private walkingService: any = null;

  /**
   * 初始化地图
   * @param containerId 地图容器ID
   * @param center 地图中心点
   * @param zoom 缩放级别
   */
  async initMap(containerId: string, center: { lng: number; lat: number }, zoom: number = 12): Promise<void> {
    return new Promise((resolve, reject) => {
      // 检查是否已加载高德地图API
      if (!window.AMap) {
        // 配置安全密钥（如果提供了）
        const securityJsCode = import.meta.env.VITE_AMAP_SECURITY_JS_CODE;
        if (securityJsCode) {
          (window as any)._AMapSecurityConfig = {
            securityJsCode: securityJsCode
          };
        }
        
        // 动态加载高德地图API
        const script = document.createElement('script');
        script.type = 'text/javascript';
        const apiKey = import.meta.env.VITE_AMAP_KEY || 'your-amap-key';
        // 不在URL中加载插件，而是使用 AMap.plugin() 方法异步加载
        script.src = `https://webapi.amap.com/maps?v=2.0&key=${apiKey}`;
        script.onload = () => {
          this.createMap(containerId, center, zoom, resolve, reject);
        };
        script.onerror = reject;
        document.head.appendChild(script);
      } else {
        this.createMap(containerId, center, zoom, resolve, reject);
      }
    });
  }

  private createMap(containerId: string, center: { lng: number; lat: number }, zoom: number, resolve: () => void, reject: (error: any) => void) {
    try {
      const container = document.getElementById(containerId);
      if (!container) {
        reject(new Error(`地图容器 ${containerId} 不存在`));
        return;
      }
      
      // 检查容器尺寸
      const width = container.offsetWidth || container.clientWidth;
      const height = container.offsetHeight || container.clientHeight;
      if (width === 0 || height === 0) {
        console.warn('地图容器尺寸为0，等待容器渲染...');
        setTimeout(() => {
          this.createMap(containerId, center, zoom, resolve, reject);
        }, 200);
        return;
      }
      
      this.map = new AMap.Map(containerId, {
        zoom: zoom,
        center: [center.lng, center.lat],
        viewMode: '3D',
        mapStyle: 'amap://styles/normal'
      });
      
      // 添加控件（如果可用）- 使用 try-catch 避免控件初始化失败影响地图加载
      try {
        if (AMap.Zoom) {
          this.map.addControl(new AMap.Zoom());
        }
      } catch (error) {
        console.warn('缩放控件初始化失败:', error);
      }
      
      try {
        if (AMap.Scale) {
          this.map.addControl(new AMap.Scale());
        }
      } catch (error) {
        console.warn('比例尺控件初始化失败:', error);
      }
      
      try {
        if (AMap.ToolBar) {
          this.map.addControl(new AMap.ToolBar());
        }
      } catch (error) {
        console.warn('工具条控件初始化失败:', error);
      }
      
      // 初始化路线规划服务的函数
      let servicesInitialized = false;
      const initRouteServices = () => {
        // 防止重复初始化
        if (servicesInitialized) {
          return;
        }
        servicesInitialized = true;
        
        // 使用 AMap.plugin() 方法异步加载插件
        let loadedCount = 0;
        const totalPlugins = 3;
        let hasResolved = false;
        
        const checkAllLoaded = () => {
          loadedCount++;
          if (loadedCount === totalPlugins && !hasResolved) {
            hasResolved = true;
            resolve();
          }
        };
        
        // 加载驾车路线规划插件
        AMap.plugin('AMap.Driving', () => {
          try {
            this.drivingService = new AMap.Driving({
              map: this.map,
              panel: null // 不显示默认面板
            });
            console.log('驾车路线规划服务初始化成功');
          } catch (error) {
            console.error('驾车路线规划服务初始化失败:', error);
          }
          checkAllLoaded();
        });
        
        // 加载公交路线规划插件（注意：应该使用 AMap.Transfer）
        AMap.plugin('AMap.Transfer', () => {
          try {
            this.transitService = new AMap.Transfer({
              map: this.map,
              panel: null,
              city: '全国' // 默认城市，可以根据需要修改
            });
            console.log('公交路线规划服务初始化成功');
          } catch (error) {
            console.error('公交路线规划服务初始化失败:', error);
          }
          checkAllLoaded();
        });
        
        // 加载步行路线规划插件
        AMap.plugin('AMap.Walking', () => {
          try {
            this.walkingService = new AMap.Walking({
              map: this.map,
              panel: null
            });
            console.log('步行路线规划服务初始化成功');
          } catch (error) {
            console.error('步行路线规划服务初始化失败:', error);
          }
          checkAllLoaded();
        });
      };
      
      // 等待地图加载完成后再初始化路线规划服务
      this.map.on('complete', () => {
        initRouteServices();
      });
      
      // 延迟初始化，确保地图已经渲染
      setTimeout(() => {
        // 如果地图已经加载完成，直接初始化服务
        // 否则等待 complete 事件
        if (this.map && !servicesInitialized) {
          initRouteServices();
        }
      }, 200);
    } catch (error) {
      reject(error);
    }
  }

  /**
   * 添加位置标记
   * @param location 位置信息
   * @param iconUrl 图标URL（可选）
   * @param isHighlighted 是否高亮显示（默认false）
   */
  addMarker(location: Location, iconUrl?: string, isHighlighted: boolean = false): any {
    if (!this.map) return null;

    // 根据是否高亮选择不同的图标颜色
    const defaultIcon = isHighlighted 
      ? new AMap.Icon({
          size: new AMap.Size(40, 40),
          image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png',
          imageSize: new AMap.Size(40, 40)
        })
      : new AMap.Icon({
          size: new AMap.Size(32, 32),
          image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
          imageSize: new AMap.Size(32, 32)
        });

    const icon = iconUrl ? {
      image: iconUrl,
      size: new AMap.Size(32, 32),
      imageSize: new AMap.Size(32, 32)
    } : defaultIcon;

    const marker = new AMap.Marker({
      position: [location.lng, location.lat],
      title: location.name,
      icon: icon,
      offset: new AMap.Pixel(-16, -32),
      zIndex: isHighlighted ? 1000 : 100
    });

    // 添加信息窗口
    const infoWindow = new AMap.InfoWindow({
      content: this.createInfoWindowContent(location),
      offset: new AMap.Pixel(0, -40)
    });

    marker.on('click', () => {
      // 关闭其他信息窗口
      this.infoWindows.forEach(window => window.close());
      infoWindow.open(this.map, marker.getPosition());
    });

    this.map.add(marker);
    this.markers.push(marker);
    this.infoWindows.push(infoWindow);
    
    return marker;
  }

  /**
   * 高亮显示指定标记
   * @param marker 要高亮的标记
   */
  highlightMarker(marker: any): void {
    if (!marker || !this.map) return;
    
    // 恢复之前高亮的标记
    if (this.currentHighlightedMarker && this.currentHighlightedMarker !== marker) {
      this.currentHighlightedMarker.setIcon(new AMap.Icon({
        size: new AMap.Size(32, 32),
        image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
        imageSize: new AMap.Size(32, 32)
      }));
      this.currentHighlightedMarker.setZIndex(100);
    }
    
    // 高亮当前标记
    marker.setIcon(new AMap.Icon({
      size: new AMap.Size(40, 40),
      image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png',
      imageSize: new AMap.Size(40, 40)
    }));
    marker.setZIndex(1000);
    
    // 将地图中心移动到标记位置
    this.map.setCenter(marker.getPosition());
    this.map.setZoom(15);
    
    this.currentHighlightedMarker = marker;
  }

  /**
   * 绘制路线（简单折线方式）
   * @param route 路线信息
   * @param color 路线颜色（默认：#1890ff）
   */
  async drawRoute(route: Route, color: string = '#1890ff'): Promise<void> {
    if (!this.map) return;

    // 清除之前的路线
    this.clearRoutes();

    const waypoints = route.waypoints || [];
    const allPoints = [route.start, ...waypoints, route.end];
    
    // 创建路径数组
    const path = allPoints.map(point => [point.lng, point.lat]);

    // 绘制折线
    const polyline = new AMap.Polyline({
      path: path,
      strokeColor: color,
      strokeWeight: 6,
      strokeOpacity: 0.8,
      strokeStyle: 'solid'
    });

    this.map.add(polyline);
    this.polylines.push(polyline);

    // 自动调整地图视野以显示完整路线
    this.map.setFitView();
  }

  /**
   * 规划路线（使用高德地图路线规划API）
   * @param locations 地点列表（按顺序）
   * @param routeType 路线类型：driving（驾车）、transit（公交）、walking（步行）
   */
  async planRoute(locations: Location[], routeType: RouteType = 'driving'): Promise<void> {
    if (!this.map || locations.length < 2) {
      throw new Error('至少需要2个地点才能规划路线');
    }

    // 检查路线规划服务是否已初始化
    if (routeType === 'driving' && !this.drivingService) {
      throw new Error('驾车路线规划服务未初始化');
    }
    if (routeType === 'transit' && !this.transitService) {
      throw new Error('公交路线规划服务未初始化');
    }
    if (routeType === 'walking' && !this.walkingService) {
      throw new Error('步行路线规划服务未初始化');
    }

    // 清除之前的路线
    this.clearRoutes();

    return new Promise((resolve, reject) => {
      const start = locations[0];
      const end = locations[locations.length - 1];
      const waypoints = locations.slice(1, -1);

      const callback = (status: string, result: any) => {
        console.log('路线规划回调:', status, result);
        
        if (status === 'complete') {
          // 路线规划成功
          try {
            // 不同路线类型的返回结构可能不同
            let routes: any[] = [];
            
            if (routeType === 'transit') {
              // 公交路线规划返回的是 plans 数组
              if (result.plans && Array.isArray(result.plans) && result.plans.length > 0) {
                routes = result.plans;
              } else if (result.routes && Array.isArray(result.routes)) {
                routes = result.routes;
              }
            } else {
              // 驾车和步行返回的是 routes 数组
              if (result.routes && Array.isArray(result.routes)) {
                routes = result.routes;
              }
            }
            
            if (routes.length === 0) {
              reject(new Error('未找到路线'));
              return;
            }
            
            const route = routes[0];
            const path: number[][] = [];
            
            // 提取路径点 - 高德地图的路线数据结构
            if (routeType === 'transit') {
              // 公交路线规划的数据结构：plans -> segments -> path
              if (route.segments && Array.isArray(route.segments)) {
                route.segments.forEach((segment: any) => {
                  if (segment.transit && segment.transit.path && Array.isArray(segment.transit.path)) {
                    segment.transit.path.forEach((point: any) => {
                      if (Array.isArray(point)) {
                        path.push([point[0], point[1]]);
                      } else if (point.lng !== undefined && point.lat !== undefined) {
                        path.push([point.lng, point.lat]);
                      } else if (point.getLng && point.getLat) {
                        path.push([point.getLng(), point.getLat()]);
                      }
                    });
                  }
                  // 步行段
                  if (segment.walking && segment.walking.path && Array.isArray(segment.walking.path)) {
                    segment.walking.path.forEach((point: any) => {
                      if (Array.isArray(point)) {
                        path.push([point[0], point[1]]);
                      } else if (point.lng !== undefined && point.lat !== undefined) {
                        path.push([point.lng, point.lat]);
                      } else if (point.getLng && point.getLat) {
                        path.push([point.getLng(), point.getLat()]);
                      }
                    });
                  }
                });
              }
              // 如果 segments 中没有路径，尝试从其他字段提取
              if (path.length === 0 && route.path && Array.isArray(route.path)) {
                route.path.forEach((point: any) => {
                  if (Array.isArray(point)) {
                    path.push([point[0], point[1]]);
                  } else if (point.lng !== undefined && point.lat !== undefined) {
                    path.push([point.lng, point.lat]);
                  } else if (point.getLng && point.getLat) {
                    path.push([point.getLng(), point.getLat()]);
                  }
                });
              }
            } else {
              // 驾车和步行路线规划的数据结构
              if (route.steps && Array.isArray(route.steps)) {
                route.steps.forEach((step: any) => {
                  if (step.path && Array.isArray(step.path)) {
                    step.path.forEach((point: any) => {
                      // 处理不同的点格式
                      if (Array.isArray(point)) {
                        // 如果是数组格式 [lng, lat]
                        path.push([point[0], point[1]]);
                      } else if (point.lng !== undefined && point.lat !== undefined) {
                        // 如果是对象格式 {lng, lat}
                        path.push([point.lng, point.lat]);
                      } else if (point.getLng && point.getLat) {
                        // 如果是 AMap.LngLat 对象
                        path.push([point.getLng(), point.getLat()]);
                      }
                    });
                  }
                });
              } else if (route.path && Array.isArray(route.path)) {
                // 如果直接有 path 属性
                route.path.forEach((point: any) => {
                  if (Array.isArray(point)) {
                    path.push([point[0], point[1]]);
                  } else if (point.lng !== undefined && point.lat !== undefined) {
                    path.push([point.lng, point.lat]);
                  } else if (point.getLng && point.getLat) {
                    path.push([point.getLng(), point.getLat()]);
                  }
                });
              }
            }

            // 如果无法提取路径点，但路线已经在地图上显示（高德地图自动绘制），仍然 resolve
            if (path.length === 0) {
              console.warn('无法提取路线路径点，但路线可能已在地图上显示');
              // 对于公交路线规划，高德地图会自动绘制路线，所以即使无法提取路径点，也应该 resolve
              if (routeType === 'transit') {
                // 调整地图视野以显示所有标记
                if (this.markers.length > 0) {
                  this.map.setFitView(this.markers, false, [50, 50, 50, 50]);
                }
                resolve();
                return;
              } else {
                reject(new Error('无法提取路线路径点'));
                return;
              }
            }

            // 绘制路线
            const polyline = new AMap.Polyline({
              path: path,
              strokeColor: this.getRouteColor(routeType),
              strokeWeight: 6,
              strokeOpacity: 0.8,
              strokeStyle: 'solid'
            });

            this.map.add(polyline);
            this.polylines.push(polyline);

            // 自动调整地图视野以显示完整路线
            this.map.setFitView([polyline], false, [50, 50, 50, 50]);
            resolve();
          } catch (error: any) {
            console.error('处理路线规划结果时出错:', error);
            reject(new Error('处理路线规划结果失败：' + (error?.message || '未知错误')));
          }
        } else if (status === 'error' || status === 'no_data') {
          const errorMsg = result?.info || result?.message || result || '未知错误';
          reject(new Error('路线规划失败：' + errorMsg));
        } else {
          // 其他状态
          const errorMsg = result?.info || result?.message || result || '未知错误';
          reject(new Error('路线规划失败：' + errorMsg));
        }
      };

      // 根据路线类型选择服务
      let service: any;
      switch (routeType) {
        case 'driving':
          service = this.drivingService;
          if (!service) {
            reject(new Error('驾车路线规划服务未初始化'));
            return;
          }
          if (waypoints.length > 0) {
            // 驾车路线支持途经点，需要传入 AMap.LngLat 对象数组
            const waypointsLngLat = waypoints.map(wp => new AMap.LngLat(wp.lng, wp.lat));
            service.search(
              new AMap.LngLat(start.lng, start.lat),
              new AMap.LngLat(end.lng, end.lat),
              {
                waypoints: waypointsLngLat
              },
              callback
            );
          } else {
            service.search(
              new AMap.LngLat(start.lng, start.lat),
              new AMap.LngLat(end.lng, end.lat),
              {},
              callback
            );
          }
          break;
        case 'transit':
          service = this.transitService;
          if (!service) {
            reject(new Error('公交路线规划服务未初始化'));
            return;
          }
          // 公交路线规划需要指定城市
          service.search(
            new AMap.LngLat(start.lng, start.lat),
            new AMap.LngLat(end.lng, end.lat),
            {
              city: '全国' // 可以根据需要修改为具体城市
            },
            callback
          );
          break;
        case 'walking':
          service = this.walkingService;
          if (!service) {
            reject(new Error('步行路线规划服务未初始化'));
            return;
          }
          // 步行路线规划不支持途经点，只支持起点和终点
          // 如果有多个地点，只使用第一个和最后一个
          try {
            console.log('开始步行路线规划:', { start, end });
            service.search(
              new AMap.LngLat(start.lng, start.lat),
              new AMap.LngLat(end.lng, end.lat),
              {},
              callback
            );
          } catch (error: any) {
            console.error('步行路线规划调用失败:', error);
            reject(new Error('步行路线规划调用失败：' + (error?.message || '未知错误')));
          }
          break;
        default:
          reject(new Error('不支持的路线类型'));
      }
    });
  }

  /**
   * 获取路线颜色
   */
  private getRouteColor(routeType: RouteType): string {
    const colorMap: Record<RouteType, string> = {
      'driving': '#409eff', // 蓝色 - 驾车
      'transit': '#67c23a', // 绿色 - 公交
      'walking': '#e6a23c'  // 橙色 - 步行
    };
    return colorMap[routeType] || '#409eff';
  }

  /**
   * 清除所有标记
   */
  clearMarkers(): void {
    this.markers.forEach(marker => {
      this.map.remove(marker);
    });
    this.markers = [];
    
    this.infoWindows.forEach(window => {
      window.close();
    });
    this.infoWindows = [];
    
    this.currentHighlightedMarker = null;
  }

  /**
   * 清除所有路线
   */
  clearRoutes(): void {
    this.polylines.forEach(polyline => {
      this.map.remove(polyline);
    });
    this.polylines = [];
  }

  /**
   * 设置地图中心点
   * @param center 中心点坐标
   */
  setCenter(center: { lng: number; lat: number }): void {
    if (this.map) {
      this.map.setCenter([center.lng, center.lat]);
    }
  }

  /**
   * 设置缩放级别
   * @param zoom 缩放级别
   */
  setZoom(zoom: number): void {
    if (this.map) {
      this.map.setZoom(zoom);
    }
  }

  /**
   * 获取地图实例
   */
  getMap(): any {
    return this.map;
  }

  /**
   * 调整地图尺寸（当容器尺寸改变时调用）
   */
  resize(): void {
    if (this.map) {
      this.map.getSize();
    }
  }

  /**
   * 销毁地图
   */
  destroy(): void {
    if (this.map) {
      this.map.destroy();
      this.map = null;
    }
    this.markers = [];
    this.polylines = [];
    this.infoWindows = [];
    this.currentHighlightedMarker = null;
    this.drivingService = null;
    this.transitService = null;
    this.walkingService = null;
  }

  /**
   * 创建信息窗口内容
   */
  private createInfoWindowContent(location: Location): string {
    return `
      <div style="padding: 8px; max-width: 200px;">
        <h4 style="margin: 0 0 8px 0; font-size: 14px; color: #303133;">${location.name}</h4>
        <p style="margin: 0 0 4px 0; font-size: 12px; color: #606266;">类型：${this.getLocationTypeName(location.type)}</p>
        ${location.description ? `<p style="margin: 0 0 4px 0; font-size: 12px; color: #606266;">${location.description}</p>` : ''}
        <p style="margin: 0; font-size: 12px; color: #909399;">经纬度：${location.lat.toFixed(6)}, ${location.lng.toFixed(6)}</p>
      </div>
    `;
  }

  /**
   * 获取位置类型的中文名称
   */
  private getLocationTypeName(type: string): string {
    const typeMap: Record<string, string> = {
      'attraction': '景点',
      'restaurant': '餐厅',
      'cultural': '文化场所',
      'shopping': '购物',
      'hotel': '酒店'
    };
    return typeMap[type] || type;
  }
}

export default MapService;
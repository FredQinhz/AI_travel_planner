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
        // 动态加载高德地图API
        const script = document.createElement('script');
        script.type = 'text/javascript';
        const apiKey = import.meta.env.VITE_AMAP_KEY || 'your-amap-key';
        script.src = `https://webapi.amap.com/maps?v=2.0&key=${apiKey}&plugin=AMap.Driving,AMap.Transit,AMap.Walking`;
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
      this.map = new AMap.Map(containerId, {
        zoom: zoom,
        center: [center.lng, center.lat],
        viewMode: '3D',
        mapStyle: 'amap://styles/normal'
      });
      
      // 添加缩放控件
      this.map.addControl(new AMap.Zoom());
      
      // 添加比例尺控件
      this.map.addControl(new AMap.Scale());
      
      // 添加工具条控件
      this.map.addControl(new AMap.ToolBar());
      
      // 初始化路线规划服务
      this.drivingService = new AMap.Driving({
        map: this.map,
        panel: null // 不显示默认面板
      });
      
      this.transitService = new AMap.Transit({
        map: this.map,
        panel: null
      });
      
      this.walkingService = new AMap.Walking({
        map: this.map,
        panel: null
      });
      
      resolve();
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
    if (!this.map || locations.length < 2) return;

    // 清除之前的路线
    this.clearRoutes();

    return new Promise((resolve, reject) => {
      const start = locations[0];
      const end = locations[locations.length - 1];
      const waypoints = locations.slice(1, -1);

      const callback = (status: string, result: any) => {
        if (status === 'complete') {
          // 路线规划成功
          if (result.routes && result.routes.length > 0) {
            const route = result.routes[0];
            const path: number[][] = [];
            
            // 提取路径点
            route.steps.forEach((step: any) => {
              step.path.forEach((point: any) => {
                path.push([point.lng, point.lat]);
              });
            });

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
            this.map.setFitView(null, false, [50, 50, 50, 50]);
            resolve();
          } else {
            reject(new Error('未找到路线'));
          }
        } else {
          reject(new Error('路线规划失败：' + result));
        }
      };

      // 根据路线类型选择服务
      let service: any;
      switch (routeType) {
        case 'driving':
          service = this.drivingService;
          if (waypoints.length > 0) {
            // 驾车路线支持途经点
            service.search(
              new AMap.LngLat(start.lng, start.lat),
              new AMap.LngLat(end.lng, end.lat),
              {
                waypoints: waypoints.map(wp => new AMap.LngLat(wp.lng, wp.lat))
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
          service.search(
            new AMap.LngLat(start.lng, start.lat),
            new AMap.LngLat(end.lng, end.lat),
            {},
            callback
          );
          break;
        case 'walking':
          service = this.walkingService;
          if (waypoints.length > 0) {
            // 步行路线支持途经点
            service.search(
              new AMap.LngLat(start.lng, start.lat),
              new AMap.LngLat(end.lng, end.lat),
              {
                waypoints: waypoints.map(wp => new AMap.LngLat(wp.lng, wp.lat))
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
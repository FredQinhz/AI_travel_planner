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

export class MapService {
  private map: any = null;
  private markers: any[] = [];
  private polylines: any[] = [];
  private infoWindows: any[] = [];

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
        script.src = `https://webapi.amap.com/maps?v=2.0&key=${import.meta.env.VITE_AMAP_KEY || 'your-amap-key'}`;
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
      
      resolve();
    } catch (error) {
      reject(error);
    }
  }

  /**
   * 添加位置标记
   * @param location 位置信息
   * @param iconUrl 图标URL（可选）
   */
  addMarker(location: Location, iconUrl?: string): void {
    if (!this.map) return;

    const icon = iconUrl ? {
      image: iconUrl,
      size: new AMap.Size(32, 32),
      imageSize: new AMap.Size(32, 32)
    } : undefined;

    const marker = new AMap.Marker({
      position: [location.lng, location.lat],
      title: location.name,
      icon: icon,
      offset: new AMap.Pixel(-16, -32)
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
  }

  /**
   * 绘制路线
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
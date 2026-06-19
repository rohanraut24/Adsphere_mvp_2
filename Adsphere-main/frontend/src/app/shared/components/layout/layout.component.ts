import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../sidebar/sidebar.component';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, SidebarComponent],
  template: `
    <div class="flex h-screen bg-gray-50 overflow-hidden">
      <app-sidebar class="h-full flex shrink-0"></app-sidebar>
      <div class="flex-1 flex flex-col min-w-0 overflow-hidden">
        <header class="h-14 bg-white border-b border-gray-100 flex items-center px-8 shrink-0">
          <h1 class="text-sm font-semibold text-gray-800">{{ getTitle() }}</h1>
          <div class="ml-auto flex items-center gap-3">
            <div class="w-2 h-2 rounded-full bg-emerald-500"></div>
            <span class="text-xs text-gray-500">Connected to backend</span>
          </div>
        </header>
        <main class="flex-1 p-8 overflow-auto">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `
})
export class LayoutComponent {
  private router = inject(Router);

  private readonly TITLES: Record<string, string> = {
    '/advertiser/campaigns/new': 'New Campaign',
    '/advertiser/campaigns': 'My Campaigns',
    '/advertiser': 'Dashboard',
    '/publisher/websites/new': 'Register Website',
    '/publisher/websites': 'My Websites',
    '/publisher/placements': 'Placements',
    '/publisher': 'Dashboard',
    '/network/websites': 'Website Approvals',
    '/network/campaigns': 'Campaign Approvals',
    '/admin/users': 'User Management',
    '/admin/upgrades': 'Upgrade Requests',
    '/admin': 'Platform Statistics',
  };

  getTitle(): string {
    const url = this.router.url;
    const sortedKeys = Object.keys(this.TITLES).sort((a, b) => b.length - a.length);
    for (const key of sortedKeys) {
      if (url.startsWith(key)) {
        return this.TITLES[key];
      }
    }
    return 'AdSphere';
  }
}

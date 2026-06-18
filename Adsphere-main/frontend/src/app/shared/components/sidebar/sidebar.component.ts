import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LucideDynamicIcon } from '@lucide/angular';

interface NavItem {
  to: string;
  label: string;
  icon: string;
  end?: boolean;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive,
    LucideDynamicIcon
  ],
  template: `
    <aside class="w-64 min-h-screen bg-gray-950 text-white flex flex-col shrink-0">
      <!-- Logo -->
      <div class="px-6 py-5 flex items-center gap-2.5 border-b border-gray-800">
        <div class="w-8 h-8 rounded-lg bg-indigo-600 flex items-center justify-center">
          <svg lucideIcon="zap" [size]="16" class="text-white"></svg>
        </div>
        <span class="text-lg font-bold tracking-tight">AdSphere</span>
      </div>

      <!-- Nav -->
      <nav class="flex-1 px-3 py-5 flex flex-col gap-0.5">
        <p class="px-3 text-[10px] font-bold text-gray-600 uppercase tracking-widest mb-2">Menu</p>
        <a
          *ngFor="let item of navItems"
          [routerLink]="[item.to]"
          [routerLinkActive]="['bg-indigo-600', 'text-white', 'shadow-lg', 'shadow-indigo-900/30']"
          [routerLinkActiveOptions]="{ exact: item.end || false }"
          class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all text-gray-400 hover:bg-gray-800 hover:text-white"
        >
          <svg [lucideIcon]="item.icon" [size]="17"></svg>
          {{ item.label }}
        </a>
      </nav>

      <!-- User -->
      <div class="px-4 py-4 border-t border-gray-800">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-9 h-9 rounded-full bg-indigo-600 flex items-center justify-center text-sm font-bold shrink-0">
            {{ initials }}
          </div>
          <div class="min-w-0">
            <p class="text-sm font-medium text-white truncate">{{ userEmail }}</p>
            <p class="text-xs font-semibold {{ roleColorClass }}">{{ roleLabel }}</p>
          </div>
        </div>
        <button
          (click)="onLogout()"
          class="flex items-center gap-2 text-xs text-gray-500 hover:text-red-400 transition-colors cursor-pointer w-full text-left"
        >
          <svg lucideIcon="log-out" [size]="13"></svg>
          Sign out
        </button>
      </div>
    </aside>
  `
})
export class SidebarComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  private readonly NAV: Record<string, NavItem[]> = {
    ADVERTISER: [
      { to: '/advertiser', label: 'Dashboard', icon: 'layout-dashboard', end: true },
      { to: '/advertiser/campaigns', label: 'Campaigns', icon: 'megaphone' },
    ],
    PUBLISHER: [
      { to: '/publisher', label: 'Dashboard', icon: 'layout-dashboard', end: true },
      { to: '/publisher/websites', label: 'Websites', icon: 'globe' },
      { to: '/publisher/placements', label: 'Placements', icon: 'layout-grid' },
    ],
    NETWORK_ADMIN: [
      { to: '/network/websites', label: 'Website Approvals', icon: 'check-square' },
      { to: '/network/campaigns', label: 'Campaign Approvals', icon: 'megaphone' },
    ],
    SUPER_ADMIN: [
      { to: '/admin', label: 'Platform Stats', icon: 'trending-up', end: true },
      { to: '/admin/users', label: 'Users', icon: 'users' },
      { to: '/admin/upgrades', label: 'Upgrade Requests', icon: 'shield-check' },
    ],
  };

  private readonly ROLE_LABEL: Record<string, string> = {
    ADVERTISER: 'Advertiser',
    PUBLISHER: 'Publisher',
    NETWORK_ADMIN: 'Network Admin',
    SUPER_ADMIN: 'Super Admin',
  };

  private readonly ROLE_COLOR: Record<string, string> = {
    ADVERTISER: 'text-orange-400',
    PUBLISHER: 'text-cyan-400',
    NETWORK_ADMIN: 'text-indigo-400',
    SUPER_ADMIN: 'text-violet-400',
  };

  get user() {
    return this.authService.currentUser;
  }

  get navItems(): NavItem[] {
    return this.user ? this.NAV[this.user.role] || [] : [];
  }

  get initials(): string {
    return this.user?.email ? this.user.email.slice(0, 2).toUpperCase() : 'AD';
  }

  get userEmail(): string {
    return this.user?.email || '';
  }

  get roleLabel(): string {
    return this.user ? this.ROLE_LABEL[this.user.role] || '' : '';
  }

  get roleColorClass(): string {
    return this.user ? this.ROLE_COLOR[this.user.role] || '' : '';
  }

  onLogout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}

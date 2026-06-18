import { Routes } from '@angular/router';
import { authGuard, noAuthGuard } from './core/guards/auth.guard';
import { LayoutComponent } from './shared/components/layout/layout.component';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [noAuthGuard],
    loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    canActivate: [noAuthGuard],
    loadComponent: () => import('./pages/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'advertiser',
        canActivate: [authGuard],
        data: { role: 'ADVERTISER' },
        children: [
          { path: '', loadComponent: () => import('./pages/advertiser/dashboard/dashboard.component').then(m => m.DashboardComponent) },
          { path: 'campaigns', loadComponent: () => import('./pages/advertiser/campaign-list/campaign-list.component').then(m => m.CampaignListComponent) },
          { path: 'campaigns/new', loadComponent: () => import('./pages/advertiser/campaign-form/campaign-form.component').then(m => m.CampaignFormComponent) },
          { path: 'campaigns/:id', loadComponent: () => import('./pages/advertiser/campaign-detail/campaign-detail.component').then(m => m.CampaignDetailComponent) },
          { path: 'campaigns/:id/edit', loadComponent: () => import('./pages/advertiser/campaign-form/campaign-form.component').then(m => m.CampaignFormComponent) },
        ]
      },
      {
        path: 'publisher',
        canActivate: [authGuard],
        data: { role: 'PUBLISHER' },
        children: [
          { path: '', loadComponent: () => import('./pages/publisher/dashboard/dashboard.component').then(m => m.DashboardComponent) },
          { path: 'websites', loadComponent: () => import('./pages/publisher/website-list/website-list.component').then(m => m.WebsiteListComponent) },
          { path: 'websites/new', loadComponent: () => import('./pages/publisher/website-form/website-form.component').then(m => m.WebsiteFormComponent) },
          { path: 'websites/:id', loadComponent: () => import('./pages/publisher/website-detail/website-detail.component').then(m => m.WebsiteDetailComponent) },
          { path: 'websites/:id/edit', loadComponent: () => import('./pages/publisher/website-form/website-form.component').then(m => m.WebsiteFormComponent) },
          { path: 'placements', loadComponent: () => import('./pages/publisher/placements/placements.component').then(m => m.PlacementsComponent) },
        ]
      },
      {
        path: 'network',
        canActivate: [authGuard],
        data: { role: 'NETWORK_ADMIN' },
        children: [
          { path: '', loadComponent: () => import('./pages/network/dashboard/dashboard.component').then(m => m.DashboardComponent) },
          { path: 'websites', loadComponent: () => import('./pages/network/pending-websites/pending-websites.component').then(m => m.PendingWebsitesComponent) },
          { path: 'campaigns', loadComponent: () => import('./pages/network/pending-campaigns/pending-campaigns.component').then(m => m.PendingCampaignsComponent) },
        ]
      },
      {
        path: 'admin',
        canActivate: [authGuard],
        data: { role: 'SUPER_ADMIN' },
        children: [
          { path: '', loadComponent: () => import('./pages/admin/stats/stats.component').then(m => m.StatsComponent) },
          { path: 'users', loadComponent: () => import('./pages/admin/users/users.component').then(m => m.UsersComponent) },
          { path: 'upgrades', loadComponent: () => import('./pages/admin/upgrades/upgrades.component').then(m => m.UpgradesComponent) },
        ]
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'advertiser'
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];

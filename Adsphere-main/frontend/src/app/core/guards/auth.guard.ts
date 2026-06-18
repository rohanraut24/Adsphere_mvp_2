import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const user = authService.currentUser;

  if (!user) {
    router.navigate(['/login']);
    return false;
  }

  const expectedRole = route.data['role'];
  if (expectedRole && user.role !== expectedRole) {
    router.navigate(['/login']);
    return false;
  }

  return true;
};

export const noAuthGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const user = authService.currentUser;

  if (user) {
    const ROLE_REDIRECT: Record<string, string> = {
      SUPER_ADMIN: '/admin',
      NETWORK_ADMIN: '/network/websites',
      PUBLISHER: '/publisher',
      ADVERTISER: '/advertiser',
    };
    router.navigate([ROLE_REDIRECT[user.role] || '/advertiser']);
    return false;
  }
  return true;
};

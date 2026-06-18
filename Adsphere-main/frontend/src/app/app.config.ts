import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { provideCharts, withDefaultRegisterables } from 'ng2-charts';
import {
  provideLucideIcons,
  LucideLayoutDashboard, LucideMegaphone, LucideGlobe, LucideLayoutGrid, LucideCheckSquare, LucideUsers, LucideTrendingUp,
  LucideLogOut, LucideZap, LucideShieldCheck, LucideX, LucideEye, LucideMousePointerClick, LucidePercent, LucideDollarSign,
  LucidePlay, LucidePause, LucideSend, LucideTrash2, LucideArrowLeft, LucideEdit, LucidePlus, LucideImage, LucideToggleLeft, LucideToggleRight,
  LucideShieldAlert, LucideArrowRight, LucideMail, LucideLock, LucideUser, LucideCheckCircle, LucideBarChart3, LucideClock, LucideSave,
  LucideArrowUpCircle, LucideSearch, LucideUserX
} from '@lucide/angular';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideCharts(withDefaultRegisterables()),
    provideLucideIcons(
      LucideLayoutDashboard, LucideMegaphone, LucideGlobe, LucideLayoutGrid, LucideCheckSquare, LucideUsers, LucideTrendingUp,
      LucideLogOut, LucideZap, LucideShieldCheck, LucideX, LucideEye, LucideMousePointerClick, LucidePercent, LucideDollarSign,
      LucidePlay, LucidePause, LucideSend, LucideTrash2, LucideArrowLeft, LucideEdit, LucidePlus, LucideImage, LucideToggleLeft, LucideToggleRight,
      LucideShieldAlert, LucideArrowRight, LucideMail, LucideLock, LucideUser, LucideCheckCircle, LucideBarChart3, LucideClock, LucideSave,
      LucideArrowUpCircle, LucideSearch, LucideUserX
    )
  ]
};

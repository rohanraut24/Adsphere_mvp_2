import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { LucideDynamicIcon } from '@lucide/angular';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule, LucideDynamicIcon],
  template: `
    <div class="min-h-screen flex">
      <!-- Left panel -->
      <div class="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-indigo-700 via-indigo-600 to-violet-600 flex-col justify-between p-12 relative overflow-hidden">
        <div class="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,_rgba(255,255,255,0.1),_transparent_60%)]"></div>
        <div class="flex items-center gap-3 relative">
          <div class="w-9 h-9 rounded-xl bg-white/20 flex items-center justify-center">
            <svg lucideIcon="zap" [size]="18" class="text-white"></svg>
          </div>
          <span class="text-xl font-bold text-white tracking-tight">AdSphere</span>
        </div>
        <div class="relative">
          <h2 class="text-4xl font-bold text-white leading-tight mb-4">
            The complete ad management platform
          </h2>
          <p class="text-indigo-200 text-base leading-relaxed">
            Manage campaigns, track analytics, and grow your revenue — all in one place.
          </p>
          <div class="mt-10 grid grid-cols-2 gap-4">
            <div *ngFor="let benefit of benefits" class="bg-white/10 rounded-xl p-4 backdrop-blur-sm">
              <p class="text-white font-semibold text-sm">{{ benefit.role }}</p>
              <p class="text-indigo-200 text-xs mt-1">{{ benefit.desc }}</p>
            </div>
          </div>
        </div>
        <p class="text-indigo-300 text-xs relative">© 2024 AdSphere. All rights reserved.</p>
      </div>

      <!-- Right panel -->
      <div class="flex-1 flex items-center justify-center p-8 bg-gray-50">
        <div class="w-full max-w-sm">
          <div class="lg:hidden flex items-center gap-2 mb-8">
            <div class="w-8 h-8 rounded-xl bg-indigo-600 flex items-center justify-center">
              <svg lucideIcon="zap" [size]="15" class="text-white"></svg>
            </div>
            <span class="text-lg font-bold text-gray-900">AdSphere</span>
          </div>

          <h1 class="text-2xl font-bold text-gray-900 mb-1">Welcome back</h1>
          <p class="text-gray-500 text-sm mb-8">Sign in to your account to continue</p>

          <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-4">
            <div>
              <label class="label">Email address</label>
              <div class="relative">
                <svg lucideIcon="mail" [size]="15" class="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none"></svg>
                <input formControlName="email" placeholder="you@example.com" class="input-icon" />
              </div>
              <div *ngIf="form.get('email')?.touched && form.get('email')?.invalid" class="text-red-500 text-xs mt-1.5">
                <p *ngIf="form.get('email')?.errors?.['required']">Email address is required.</p>
                <p *ngIf="form.get('email')?.errors?.['email']">Enter a valid email address.</p>
              </div>
            </div>

            <div>
              <label class="label">Password</label>
              <div class="relative">
                <svg lucideIcon="lock" [size]="15" class="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none"></svg>
                <input formControlName="password" type="password" placeholder="••••••••" class="input-icon" />
              </div>
              <div *ngIf="form.get('password')?.touched && form.get('password')?.invalid" class="text-red-500 text-xs mt-1.5">
                <p *ngIf="form.get('password')?.errors?.['required']">Password is required.</p>
              </div>
            </div>

            <div *ngIf="errorMsg" class="bg-red-50 border border-red-200 rounded-xl px-4 py-3">
              <p class="text-red-600 text-sm">{{ errorMsg }}</p>
            </div>

            <button type="submit" [disabled]="form.invalid || isSubmitting" class="btn-primary w-full py-2.5 flex items-center justify-center gap-2">
              <span *ngIf="isSubmitting" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
              <ng-container *ngIf="!isSubmitting">
                Sign In <svg lucideIcon="arrow-right" [size]="15"></svg>
              </ng-container>
            </button>
          </form>

          <p class="text-sm text-gray-500 mt-6 text-center">
            Don't have an account? 
            <a routerLink="/register" class="text-indigo-600 font-semibold hover:underline">Create one</a>
          </p>

          <div class="mt-8 p-4 bg-amber-50 border border-amber-200 rounded-xl">
            <p class="text-xs font-semibold text-amber-800 mb-2">Demo accounts</p>
            <div class="space-y-1 text-xs text-amber-700">
              <p>All new registrations default to <strong>Advertiser</strong></p>
              <p>Request role upgrade from your dashboard</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class LoginComponent {
  form: FormGroup;
  isSubmitting = false;
  errorMsg = '';

  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private router = inject(Router);

  benefits = [
    { role: 'Publishers', desc: '70% revenue share' },
    { role: 'Advertisers', desc: 'CPC-based campaigns' },
    { role: 'Network Admins', desc: 'Full approval control' },
    { role: 'Super Admins', desc: 'Platform-wide oversight' },
  ];

  private readonly ROLE_REDIRECT: Record<string, string> = {
    SUPER_ADMIN: '/admin',
    NETWORK_ADMIN: '/network/websites',
    PUBLISHER: '/publisher',
    ADVERTISER: '/advertiser',
  };

  constructor() {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  onSubmit() {
    if (this.form.invalid) return;
    this.isSubmitting = true;
    this.errorMsg = '';

    this.api.auth.login(this.form.value).subscribe({
      next: (res) => {
        this.auth.login(res);
        this.isSubmitting = false;
        this.router.navigate([this.ROLE_REDIRECT[res.role] || '/advertiser']);
      },
      error: (err) => {
        this.errorMsg = err.error?.error || 'Invalid email or password';
        this.isSubmitting = false;
      }
    });
  }
}

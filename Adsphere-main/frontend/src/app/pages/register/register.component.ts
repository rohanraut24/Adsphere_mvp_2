import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { LucideDynamicIcon } from '@lucide/angular';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule, LucideDynamicIcon],
  template: `
    <div class="min-h-screen flex">
      <!-- Left panel -->
      <div class="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-violet-700 via-indigo-600 to-indigo-700 flex-col justify-between p-12 relative overflow-hidden">
        <div class="absolute inset-0 bg-[radial-gradient(ellipse_at_bottom_left,_rgba(255,255,255,0.1),_transparent_60%)]"></div>
        <div class="flex items-center gap-3 relative">
          <div class="w-9 h-9 rounded-xl bg-white/20 flex items-center justify-center">
            <svg lucideIcon="zap" [size]="18" class="text-white"></svg>
          </div>
          <span class="text-xl font-bold text-white tracking-tight">AdSphere</span>
        </div>
        <div class="relative">
          <h2 class="text-4xl font-bold text-white leading-tight mb-4">
            Start monetizing your traffic today
          </h2>
          <p class="text-indigo-200 text-base mb-10">
            Join thousands of publishers and advertisers on the AdSphere network.
          </p>
          <div class="space-y-4">
            <div *ngFor="let point of points" class="flex items-center gap-3">
              <svg lucideIcon="check-circle" [size]="16" class="text-emerald-400 shrink-0"></svg>
              <span class="text-indigo-100 text-sm">{{ point }}</span>
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

          <h1 class="text-2xl font-bold text-gray-900 mb-1">Create your account</h1>
          <p class="text-gray-500 text-sm mb-8">You'll start as an Advertiser by default</p>

          <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-4">
            <div>
              <label class="label">Full name</label>
              <div class="relative">
                <svg lucideIcon="user" [size]="15" class="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none"></svg>
                <input formControlName="fullName" placeholder="John Doe" class="input-icon" />
              </div>
              <div *ngIf="form.get('fullName')?.touched && form.get('fullName')?.invalid" class="text-red-500 text-xs mt-1.5">
                <p *ngIf="form.get('fullName')?.errors?.['required']">Full name is required.</p>
                <p *ngIf="form.get('fullName')?.errors?.['minlength']">At least 2 characters.</p>
              </div>
            </div>

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
                <input formControlName="password" type="password" placeholder="Min. 6 characters" class="input-icon" />
              </div>
              <div *ngIf="form.get('password')?.touched && form.get('password')?.invalid" class="text-red-500 text-xs mt-1.5">
                <p *ngIf="form.get('password')?.errors?.['required']">Password is required.</p>
                <p *ngIf="form.get('password')?.errors?.['minlength']">At least 6 characters.</p>
              </div>
            </div>

            <div *ngIf="errorMsg" class="bg-red-50 border border-red-200 rounded-xl px-4 py-3">
              <p class="text-red-600 text-sm">{{ errorMsg }}</p>
            </div>

            <button type="submit" [disabled]="form.invalid || isSubmitting" class="btn-primary w-full py-2.5 flex items-center justify-center gap-2">
              <span *ngIf="isSubmitting" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
              <ng-container *ngIf="!isSubmitting">
                Create Account <svg lucideIcon="arrow-right" [size]="15"></svg>
              </ng-container>
            </button>
          </form>

          <p class="text-xs text-gray-400 mt-4 text-center">
            By registering, you agree to our Terms of Service and Privacy Policy.
          </p>

          <p class="text-sm text-gray-500 mt-5 text-center">
            Already have an account? 
            <a routerLink="/login" class="text-indigo-600 font-semibold hover:underline">Sign in</a>
          </p>
        </div>
      </div>
    </div>
  `
})
export class RegisterComponent {
  form: FormGroup;
  isSubmitting = false;
  errorMsg = '';

  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private router = inject(Router);

  points = [
    'Free to register — no credit card needed',
    'Default Advertiser role, upgrade anytime',
    'Real-time analytics and revenue tracking',
    'Transparent 70/20/10 revenue split',
  ];

  constructor() {
    this.form = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit() {
    if (this.form.invalid) return;
    this.isSubmitting = true;
    this.errorMsg = '';

    this.api.auth.register(this.form.value).subscribe({
      next: (res) => {
        this.auth.login(res);
        this.isSubmitting = false;
        this.router.navigate(['/advertiser']);
      },
      error: (err) => {
        this.errorMsg = err.error?.error || 'Registration failed';
        this.isSubmitting = false;
      }
    });
  }
}

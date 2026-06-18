import { Component, Input, Output, EventEmitter, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { LucideDynamicIcon } from '@lucide/angular';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-upgrade-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideDynamicIcon],
  template: `
    <div class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" (click)="close.emit()"></div>
      <div class="relative bg-white rounded-2xl shadow-2xl w-full max-w-md p-6 z-10">
        <button (click)="close.emit()" class="absolute top-4 right-4 p-1.5 rounded-lg hover:bg-gray-100 text-gray-400 hover:text-gray-600 transition cursor-pointer">
          <svg lucideIcon="x" [size]="16"></svg>
        </button>

        <div *ngIf="success; else formContent" class="flex flex-col items-center py-6 text-center">
          <div class="w-14 h-14 rounded-full bg-emerald-100 flex items-center justify-center mb-4">
            <svg lucideIcon="shield-check" [size]="28" class="text-emerald-600"></svg>
          </div>
          <h2 class="text-lg font-bold text-gray-900 mb-2">Request Submitted!</h2>
          <p class="text-sm text-gray-500 mb-6">
            Your upgrade request to <strong>{{ getDisplayRole(targetRole) }}</strong> has been sent to the Super Admin for review. You'll be notified once a decision is made.
          </p>
          <button (click)="close.emit()" class="btn-primary px-6">Done</button>
        </div>

        <ng-template #formContent>
          <div class="flex items-center gap-3 mb-5">
            <div class="w-10 h-10 rounded-xl bg-violet-100 flex items-center justify-center">
              <svg lucideIcon="shield-check" [size]="20" class="text-violet-600"></svg>
            </div>
            <div>
              <h2 class="text-lg font-bold text-gray-900">Request Role Upgrade</h2>
              <p class="text-xs text-gray-500">Current: <strong>{{ currentRole }}</strong> → Requesting: <strong>{{ getDisplayRole(targetRole) }}</strong></p>
            </div>
          </div>

          <div class="bg-indigo-50 border border-indigo-100 rounded-xl p-4 mb-5 text-sm text-indigo-700">
            <p *ngIf="targetRole === 'PUBLISHER'; else networkAdminText">
              As a <strong>Publisher</strong> you can register websites, create ad placements, and earn <strong>70% revenue</strong> on every click.
            </p>
            <ng-template #networkAdminText>
              <p>As a <strong>Network Admin</strong> you can approve/reject publisher websites and advertiser campaigns across the network.</p>
            </ng-template>
          </div>

          <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-4">
            <div>
              <label class="label">Why do you want this role? *</label>
              <textarea
                formControlName="reason"
                rows="4"
                [placeholder]="targetRole === 'PUBLISHER'
                  ? 'e.g. I own a tech blog at example.com with 10k monthly visitors and want to monetize it through AdSphere...'
                  : 'e.g. I have 3 years of experience managing ad networks and want to help grow the AdSphere ecosystem...'
                "
                class="input resize-none"
              ></textarea>
              <div *ngIf="form.get('reason')?.touched && form.get('reason')?.invalid" class="text-red-500 text-xs mt-1.5">
                <p *ngIf="form.get('reason')?.errors?.['required']">Please provide a reason explaining your request.</p>
                <p *ngIf="form.get('reason')?.errors?.['minlength']">Please provide at least 20 characters explaining your request.</p>
              </div>
            </div>

            <div *ngIf="errorMsg" class="bg-red-50 border border-red-200 rounded-xl px-4 py-3">
              <p class="text-red-600 text-sm">{{ errorMsg }}</p>
            </div>

            <div class="flex gap-3 pt-1">
              <button type="submit" [disabled]="form.invalid || isSubmitting" class="btn-primary flex-1 flex items-center justify-center gap-2">
                <span *ngIf="isSubmitting" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                <svg *ngIf="!isSubmitting" lucideIcon="shield-check" [size]="14"></svg>
                {{ isSubmitting ? 'Submitting…' : 'Submit Request' }}
              </button>
              <button type="button" (click)="close.emit()" class="btn-secondary">Cancel</button>
            </div>
          </form>
        </ng-template>
      </div>
    </div>
  `
})
export class UpgradeRequestModalComponent implements OnInit {
  @Input() currentRole: string = '';
  @Output() close = new EventEmitter<void>();

  targetRole: string = '';
  success: boolean = false;
  isSubmitting: boolean = false;
  errorMsg: string = '';
  form!: FormGroup;

  private fb = inject(FormBuilder);
  private api = inject(ApiService);

  ngOnInit() {
    this.targetRole = this.currentRole === 'ADVERTISER' ? 'PUBLISHER' : 'NETWORK_ADMIN';
    this.form = this.fb.group({
      reason: ['', [Validators.required, Validators.minLength(20)]]
    });
  }

  getDisplayRole(role: string): string {
    return role ? role.replace(/_/g, ' ') : '';
  }

  onSubmit() {
    if (this.form.invalid) return;
    this.isSubmitting = true;
    this.errorMsg = '';
    this.api.publisher.createUpgradeRequest({
      requestedRole: this.targetRole,
      reason: this.form.value.reason
    }).subscribe({
      next: () => {
        this.success = true;
        this.isSubmitting = false;
      },
      error: (err) => {
        this.errorMsg = err.error?.error || 'Failed to submit request';
        this.isSubmitting = false;
      }
    });
  }
}

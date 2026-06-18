import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold {{ getStyle() }}">
      <span *ngIf="getDot()" class="w-1.5 h-1.5 rounded-full {{ getDot() }}"></span>
      {{ getLabel() }}
    </span>
  `
})
export class BadgeComponent {
  @Input() status: string = '';

  private readonly STATUS_STYLES: Record<string, string> = {
    DRAFT:            'bg-gray-100 text-gray-600 ring-1 ring-gray-200',
    PENDING_APPROVAL: 'bg-amber-50 text-amber-700 ring-1 ring-amber-200',
    PENDING:          'bg-amber-50 text-amber-700 ring-1 ring-amber-200',
    ACTIVE:           'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200',
    APPROVED:         'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200',
    PAUSED:           'bg-blue-50 text-blue-700 ring-1 ring-blue-200',
    COMPLETED:        'bg-purple-50 text-purple-700 ring-1 ring-purple-200',
    REJECTED:         'bg-red-50 text-red-600 ring-1 ring-red-200',
    SUSPENDED:        'bg-red-50 text-red-600 ring-1 ring-red-200',
    INACTIVE:         'bg-gray-100 text-gray-500 ring-1 ring-gray-200',
    SUPER_ADMIN:      'bg-violet-50 text-violet-700 ring-1 ring-violet-200',
    NETWORK_ADMIN:    'bg-indigo-50 text-indigo-700 ring-1 ring-indigo-200',
    PUBLISHER:        'bg-cyan-50 text-cyan-700 ring-1 ring-cyan-200',
    ADVERTISER:       'bg-orange-50 text-orange-700 ring-1 ring-orange-200',
  };

  private readonly DOTS: Record<string, string> = {
    ACTIVE: 'bg-emerald-500', APPROVED: 'bg-emerald-500',
    PAUSED: 'bg-blue-500', PENDING: 'bg-amber-500', PENDING_APPROVAL: 'bg-amber-500',
    REJECTED: 'bg-red-500', SUSPENDED: 'bg-red-500', COMPLETED: 'bg-purple-500',
  };

  getStyle(): string {
    return this.STATUS_STYLES[this.status] || 'bg-gray-100 text-gray-600';
  }

  getDot(): string {
    return this.DOTS[this.status] || '';
  }

  getLabel(): string {
    return this.status ? this.status.replace(/_/g, ' ') : '';
  }
}

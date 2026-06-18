import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideDynamicIcon } from '@lucide/angular';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  imports: [CommonModule, LucideDynamicIcon],
  template: `
    <div class="card p-5 flex items-start gap-4 hover:shadow-md transition-shadow">
      <div *ngIf="icon" class="p-2.5 rounded-xl {{ getColorClass() }}">
        <svg [lucideIcon]="icon" [size]="20"></svg>
      </div>
      <div class="flex-1 min-w-0">
        <p class="text-xs font-medium text-gray-500 uppercase tracking-wide mb-1">{{ label }}</p>
        <p class="text-2xl font-bold text-gray-900 leading-none">{{ value }}</p>
        <p *ngIf="sub" class="text-xs text-gray-400 mt-1">{{ sub }}</p>
        <p *ngIf="trend !== undefined && trend !== null" class="text-xs mt-1 font-medium"
           [ngClass]="trend >= 0 ? 'text-emerald-600' : 'text-red-500'">
          {{ trend >= 0 ? '↑' : '↓' }} {{ Math.abs(trend) }}% vs last month
        </p>
      </div>
    </div>
  `
})
export class StatCardComponent {
  protected readonly Math = Math;
  @Input() label: string = '';
  @Input() value: any = '';
  @Input() sub?: string;
  @Input() icon?: string;
  @Input() color: string = 'indigo';
  @Input() trend?: number;

  private readonly COLOR_MAP: Record<string, string> = {
    indigo: 'bg-indigo-50 text-indigo-600',
    emerald: 'bg-emerald-50 text-emerald-600',
    amber: 'bg-amber-50 text-amber-600',
    violet: 'bg-violet-50 text-violet-600',
    cyan: 'bg-cyan-50 text-cyan-600',
    rose: 'bg-rose-50 text-rose-600',
  };

  getColorClass(): string {
    return this.COLOR_MAP[this.color] || this.COLOR_MAP['indigo'];
  }
}

import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { LucideDynamicIcon } from '@lucide/angular';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { ApiService } from '../../../core/services/api.service';
import { StatCardComponent } from '../../../shared/components/stat-card/stat-card.component';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';
import { UpgradeRequestModalComponent } from '../../../shared/components/upgrade-modal/upgrade-modal.component';

@Component({
  selector: 'app-publisher-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    LucideDynamicIcon,
    BaseChartDirective,
    StatCardComponent,
    BadgeComponent,
    UpgradeRequestModalComponent
  ],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  earnings: number | null = null;
  websites: any[] = [];
  transactions: any[] = [];
  loading = true;

  upgradeRequests: any[] = [];
  showUpgradeModal = false;

  private api = inject(ApiService);

  public lineChartData: ChartConfiguration<'line'>['data'] = {
    labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
    datasets: [
      {
        data: [12, 19, 15, 25, 22, 30, 28],
        label: 'Earnings ($)',
        fill: true,
        tension: 0.4,
        borderColor: '#10b981',
        backgroundColor: 'rgba(16, 185, 129, 0.1)'
      }
    ]
  };
  public lineChartOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false }
    },
    scales: {
      y: { beginAtZero: true, grid: { color: '#f3f4f6' }, border: { display: false } },
      x: { grid: { display: false }, border: { display: false } }
    }
  };

  get approved(): number {
    return this.websites.filter(w => w.status === 'APPROVED').length;
  }

  get pending(): number {
    return this.websites.filter(w => w.status === 'PENDING').length;
  }

  get hasPendingUpgrade(): boolean {
    return this.upgradeRequests.some(r => r.status === 'PENDING');
  }

  ngOnInit() {
    this.loadData();
    this.loadUpgradeRequests();
  }

  loadData() {
    this.loading = true;
    
    const today = new Date();
    const lastWeek = new Date();
    lastWeek.setDate(today.getDate() - 6);
    
    const fromStr = lastWeek.toISOString().split('T')[0];
    const toStr = today.toISOString().split('T')[0];

    forkJoin({
      earnings: this.api.publisher.getEarnings(),
      websites: this.api.publisher.getWebsites(),
      transactions: this.api.publisher.getTransactions(),
      analytics: this.api.publisher.getGlobalDailyAnalytics(fromStr, toStr)
    }).subscribe({
      next: (res) => {
        this.earnings = res.earnings;
        this.websites = res.websites;
        this.transactions = res.transactions;
        
        // Update chart data
        if (res.analytics && res.analytics.length > 0) {
          this.lineChartData = {
            labels: res.analytics.map((a: any) => new Date(a.date).toLocaleDateString('en-US', { weekday: 'short' })),
            datasets: [
              {
                data: res.analytics.map((a: any) => a.revenue),
                label: 'Earnings ($)',
                fill: true,
                tension: 0.4,
                borderColor: '#10b981',
                backgroundColor: 'rgba(16, 185, 129, 0.1)'
              }
            ]
          };
        }
        
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  loadUpgradeRequests() {
    this.api.publisher.getUpgradeRequests().subscribe({
      next: (res) => {
        this.upgradeRequests = res;
      },
      error: () => {}
    });
  }
}

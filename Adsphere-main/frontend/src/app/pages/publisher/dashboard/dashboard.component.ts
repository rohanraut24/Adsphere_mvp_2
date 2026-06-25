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
  walletBalance: number | null = null;
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

  public pieChartData: ChartConfiguration<'pie'>['data'] = {
    labels: [],
    datasets: [{ data: [] }]
  };
  public pieChartOptions: ChartOptions<'pie'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom' }
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
    
    this.api.publisher.getTransactions().subscribe({
      next: (transactions) => {
        this.transactions = transactions;
        
        let fromStr = '2024-01-01'; // Default fallback
        const today = new Date();
        const toStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;

        if (transactions.length > 0) {
            // Find earliest transaction date
            const earliest = new Date(Math.min(...transactions.map((t: any) => new Date(t.createdAt).getTime())));
            // Ensure from date is at least 7 days ago so the chart looks nice even if just started
            const sevenDaysAgo = new Date();
            sevenDaysAgo.setDate(today.getDate() - 6);
            if (earliest > sevenDaysAgo) {
                fromStr = `${earliest.getFullYear()}-${String(earliest.getMonth() + 1).padStart(2, '0')}-${String(earliest.getDate()).padStart(2, '0')}`;
            } else {
                fromStr = `${sevenDaysAgo.getFullYear()}-${String(sevenDaysAgo.getMonth() + 1).padStart(2, '0')}-${String(sevenDaysAgo.getDate()).padStart(2, '0')}`;
            }
        } else {
            const sevenDaysAgo = new Date();
            sevenDaysAgo.setDate(today.getDate() - 6);
            fromStr = `${sevenDaysAgo.getFullYear()}-${String(sevenDaysAgo.getMonth() + 1).padStart(2, '0')}-${String(sevenDaysAgo.getDate()).padStart(2, '0')}`;
        }

        forkJoin({
          earnings: this.api.publisher.getEarnings(),
          websites: this.api.publisher.getWebsites(),
          analytics: this.api.publisher.getGlobalDailyAnalytics(fromStr, toStr),
          wallet: this.api.wallet.getBalance()
        }).subscribe({
          next: (res) => {
            this.earnings = res.earnings;
            this.websites = res.websites;
            this.walletBalance = res.wallet.balance;
            
            // Update chart data
            if (res.analytics && res.analytics.length > 0) {
              this.lineChartData = {
                labels: res.analytics.map((a: any) => {
                   const d = new Date(a.date);
                   return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
                }),
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

        // Process Pie Chart Data
        const earningsByPlacement: { [key: string]: number } = {};
        this.transactions.forEach(t => {
          const key = `Placement #${t.placementId}`;
          earningsByPlacement[key] = (earningsByPlacement[key] || 0) + (t.publisherShare || 0);
        });
        
        if (Object.keys(earningsByPlacement).length > 0) {
          this.pieChartData = {
            labels: Object.keys(earningsByPlacement),
            datasets: [{
              data: Object.values(earningsByPlacement),
              backgroundColor: ['#6366f1', '#10b981', '#f59e0b', '#3b82f6', '#8b5cf6', '#ec4899', '#14b8a6'],
              borderWidth: 0
            }]
          };
        }
        
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    }); // closes inner forkJoin subscribe
      },
      error: () => {
        this.loading = false;
      }
    }); // closes outer transactions subscribe
  }

  loadUpgradeRequests() {
    this.api.publisher.getUpgradeRequests().subscribe({
      next: (res) => {
        this.upgradeRequests = res;
      },
      error: () => {}
    });
  }

  withdrawFunds() {
    const amountStr = prompt("Enter amount to withdraw ($):", "50");
    if (amountStr) {
      const amount = parseFloat(amountStr);
      if (!isNaN(amount) && amount > 0) {
        this.api.wallet.withdraw(amount).subscribe({
          next: (res: any) => {
            this.walletBalance = res.balance;
            alert(`Successfully withdrew $${amount.toFixed(2)} from your wallet!`);
          },
          error: (err: any) => {
            alert("Withdrawal failed: " + (err.error?.message || err.message));
          }
        });
      }
    }
  }
}

import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideDynamicIcon } from '@lucide/angular';
import { ApiService } from '../../../core/services/api.service';
import { StatCardComponent } from '../../../shared/components/stat-card/stat-card.component';

@Component({
  selector: 'app-admin-stats',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    LucideDynamicIcon,
    StatCardComponent
  ],
  templateUrl: './stats.component.html'
})
export class StatsComponent implements OnInit {
  stats: any = null;

  private api = inject(ApiService);

  ngOnInit() {
    this.api.admin.getStats().subscribe({
      next: (res) => {
        this.stats = res;
      }
    });
  }

  get revenueData(): any[] {
    if (!this.stats) return [];
    
    const platform = Number(this.stats.totalPlatformRevenue);
    const network = Number(this.stats.totalNetworkRevenue);
    const publisher = network * 3.5;
    const maxVal = Math.max(platform, network, publisher, 1);

    return [
      { name: 'Platform (10%)', value: platform, percentage: (platform / maxVal) * 100 },
      { name: 'Network (20%)', value: network, percentage: (network / maxVal) * 100 },
      { name: 'Publisher (70%)', value: publisher, percentage: (publisher / maxVal) * 100 },
    ];
  }

  get ctrPercentage(): string {
    if (!this.stats) return '0%';
    const ctr = this.stats.totalImpressions > 0 ? (this.stats.totalClicks / this.stats.totalImpressions) * 100 : 0;
    return `${ctr.toFixed(2)}%`;
  }

  get ctrBarVal(): number {
    if (!this.stats) return 0;
    return this.stats.totalImpressions > 0 ? (this.stats.totalClicks / this.stats.totalImpressions) * 100 : 0;
  }

  get campaignsPerUser(): string {
    if (!this.stats) return '0';
    const val = this.stats.totalUsers > 0 ? (this.stats.totalCampaigns / this.stats.totalUsers) : 0;
    return val.toFixed(1);
  }

  get campaignsPerUserBarVal(): number {
    if (!this.stats) return 0;
    const val = this.stats.totalUsers > 0 ? (this.stats.totalCampaigns / this.stats.totalUsers) : 0;
    return Math.min(val * 20, 100);
  }

  get placementsPerWebsite(): string {
    if (!this.stats) return '0';
    const val = this.stats.totalWebsites > 0 ? (this.stats.totalPlacements / this.stats.totalWebsites) : 0;
    return val.toFixed(1);
  }

  get placementsPerWebsiteBarVal(): number {
    if (!this.stats) return 0;
    const val = this.stats.totalWebsites > 0 ? (this.stats.totalPlacements / this.stats.totalWebsites) : 0;
    return Math.min(val * 20, 100);
  }
}

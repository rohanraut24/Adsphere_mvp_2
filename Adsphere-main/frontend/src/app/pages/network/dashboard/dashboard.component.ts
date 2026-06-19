import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule, Globe, Megaphone, ArrowRight, DollarSign } from 'lucide-angular';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule, BaseChartDirective],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  pendingWebsites: any[] = [];
  pendingCampaigns: any[] = [];
  stats: any = null;
  loading = true;

  public pieChartData: ChartConfiguration<'pie'>['data'] = {
    labels: ['Network Publisher Share', 'Platform Fee Share'],
    datasets: [{ data: [0, 0] }]
  };
  public pieChartOptions: ChartOptions<'pie'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom' }
    }
  };

  private api = inject(ApiService);

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.api.network.getStats().subscribe(res => {
      this.stats = res;
      if (res && res.totalRevenue !== undefined) {
          // If totalRevenue is missing but platform/network are present
      }
      if (res) {
          this.pieChartData = {
              labels: ['Network Publisher Share', 'Platform Fee Share'],
              datasets: [{
                  data: [res.networkRevenue || 0, res.platformRevenue || 0],
                  backgroundColor: ['#10b981', '#3b82f6'],
                  borderWidth: 0
              }]
          };
      }
    });
    this.api.network.getPendingWebsites().subscribe(res => this.pendingWebsites = res);
    this.api.network.getPendingCampaigns().subscribe(res => this.pendingCampaigns = res);
  }
}

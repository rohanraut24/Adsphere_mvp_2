import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule, Globe, Megaphone, ArrowRight } from 'lucide-angular';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  pendingWebsites: any[] = [];
  pendingCampaigns: any[] = [];
  loading = true;

  private api = inject(ApiService);

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.api.network.getPendingWebsites().subscribe(res => this.pendingWebsites = res);
    this.api.network.getPendingCampaigns().subscribe(res => this.pendingCampaigns = res);
  }
}

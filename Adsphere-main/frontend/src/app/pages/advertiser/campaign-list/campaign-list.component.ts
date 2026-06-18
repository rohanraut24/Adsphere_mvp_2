import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LucideDynamicIcon } from '@lucide/angular';
import { ApiService } from '../../../core/services/api.service';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-campaign-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    LucideDynamicIcon,
    BadgeComponent
  ],
  templateUrl: './campaign-list.component.html'
})
export class CampaignListComponent implements OnInit {
  campaigns: any[] = [];
  search = '';
  filter = 'ALL';

  STATUSES = ['ALL', 'DRAFT', 'PENDING_APPROVAL', 'ACTIVE', 'PAUSED', 'COMPLETED', 'REJECTED'];

  private api = inject(ApiService);

  ngOnInit() {
    this.load();
  }

  load() {
    this.api.advertiser.getCampaigns().subscribe({
      next: (res) => {
        this.campaigns = res;
      }
    });
  }

  get visibleCampaigns(): any[] {
    return this.campaigns
      .filter(c => this.filter === 'ALL' || c.status === this.filter)
      .filter(c => c.name.toLowerCase().includes(this.search.toLowerCase()));
  }

  action(apiFn: (id: any) => Observable<any>, id: any) {
    apiFn(id).subscribe({
      next: () => {
        this.load();
      }
    });
  }

  get submitCampaignFn() { return this.api.advertiser.submitCampaign.bind(this.api.advertiser); }
  get deleteCampaignFn() { return this.api.advertiser.deleteCampaign.bind(this.api.advertiser); }
  get pauseCampaignFn() { return this.api.advertiser.pauseCampaign.bind(this.api.advertiser); }
  get resumeCampaignFn() { return this.api.advertiser.resumeCampaign.bind(this.api.advertiser); }
}

import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideDynamicIcon } from '@lucide/angular';
import { ApiService } from '../../../core/services/api.service';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-pending-campaigns',
  standalone: true,
  imports: [
    CommonModule,
    LucideDynamicIcon,
    BadgeComponent
  ],
  templateUrl: './pending-campaigns.component.html'
})
export class PendingCampaignsComponent implements OnInit {
  campaigns: any[] = [];
  loading = true;

  private api = inject(ApiService);

  ngOnInit() {
    this.load();
  }

  load() {
    this.api.network.getPendingCampaigns().subscribe({
      next: (res) => {
        this.campaigns = res;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  action(apiFn: (id: any) => Observable<any>, id: any) {
    apiFn(id).subscribe({
      next: () => {
        this.load();
      }
    });
  }

  get approveCampaignFn() { return this.api.network.approveCampaign.bind(this.api.network); }
  get rejectCampaignFn() { return this.api.network.rejectCampaign.bind(this.api.network); }
}

import { Component, inject, OnInit } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LucideDynamicIcon } from '@lucide/angular';
import { ApiService } from '../../../core/services/api.service';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';

@Component({
  selector: 'app-campaign-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    LucideDynamicIcon,
    BadgeComponent
  ],
  templateUrl: './campaign-detail.component.html'
})
export class CampaignDetailComponent implements OnInit {
  campaign: any = null;
  creatives: any[] = [];
  placements: any[] = [];
  analytics: any = null;
  daily: any[] = [];

  today = `${new Date().getFullYear()}-${String(new Date().getMonth() + 1).padStart(2, '0')}-${String(new Date().getDate()).padStart(2, '0')}`;
  monthAgo = `${new Date(Date.now() - 30 * 864e5).getFullYear()}-${String(new Date(Date.now() - 30 * 864e5).getMonth() + 1).padStart(2, '0')}-${String(new Date(Date.now() - 30 * 864e5).getDate()).padStart(2, '0')}`;

  EMPTY_CREATIVE = { title: '', description: '', imageUrl: '', destinationUrl: '' };
  newCreative = { ...this.EMPTY_CREATIVE };
  showCreativeForm = false;

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private location = inject(Location);
  private api = inject(ApiService);

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadCampaign(id);
    }
  }

  loadCampaign(id: string) {
    this.api.advertiser.getCampaign(id).subscribe({
      next: (res) => {
        this.campaign = res;
      }
    });

    this.api.advertiser.getCreatives(id).subscribe({
      next: (res) => {
        this.creatives = res;
      }
    });

    this.api.advertiser.getPlacements(id).subscribe({
      next: (res) => {
        this.placements = res;
      }
    });

    this.api.advertiser.getAnalytics(id, this.monthAgo, this.today).subscribe({
      next: (res) => {
        this.analytics = res;
      },
      error: () => {}
    });

    this.api.advertiser.getDailyAnalytics(id, this.monthAgo, this.today).subscribe({
      next: (res) => {
        this.daily = res;
      },
      error: () => {}
    });
  }

  refreshCampaign() {
    if (this.campaign?.id) {
      this.api.advertiser.getCampaign(this.campaign.id).subscribe({
        next: (res) => {
          this.campaign = res;
        }
      });
    }
  }

  goBack() {
    this.location.back();
  }

  addCreative(e: Event) {
    e.preventDefault();
    if (!this.campaign?.id) return;

    this.api.advertiser.addCreative(this.campaign.id, this.newCreative).subscribe({
      next: () => {
        this.api.advertiser.getCreatives(this.campaign.id).subscribe(res => {
          this.creatives = res;
        });
        this.newCreative = { ...this.EMPTY_CREATIVE };
        this.showCreativeForm = false;
      }
    });
  }

  deleteCreative(cid: any, rid: any) {
    this.api.advertiser.deleteCreative(cid, rid).subscribe({
      next: () => {
        this.creatives = this.creatives.filter(c => c.id !== rid);
      }
    });
  }

  submitCampaign() {
    if (!this.campaign?.id) return;
    this.api.advertiser.submitCampaign(this.campaign.id).subscribe(() => this.refreshCampaign());
  }

  pauseCampaign() {
    if (!this.campaign?.id) return;
    this.api.advertiser.pauseCampaign(this.campaign.id).subscribe(() => this.refreshCampaign());
  }

  resumeCampaign() {
    if (!this.campaign?.id) return;
    this.api.advertiser.resumeCampaign(this.campaign.id).subscribe(() => this.refreshCampaign());
  }

  getLinePath(data: any[], key: string, width: number, height: number): string {
    if (!data || data.length === 0) return '';
    const maxVal = Math.max(...data.map(d => d[key] || 0), 1);
    const stepX = width / (data.length - 1 || 1);
    const padding = 10;
    const chartHeight = height - padding * 2;
    
    return data.map((d, i) => {
      const x = i * stepX;
      const y = height - padding - ((d[key] || 0) / maxVal) * chartHeight;
      return `${i === 0 ? 'M' : 'L'} ${x} ${y}`;
    }).join(' ');
  }

  getMidpointDate(): string {
    if (this.daily.length < 2) return '';
    const midIndex = Math.floor(this.daily.length / 2);
    return this.daily[midIndex]?.date || '';
  }
}

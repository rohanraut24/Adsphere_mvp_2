import { Component, inject, OnInit } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LucideDynamicIcon } from '@lucide/angular';
import { ApiService } from '../../../core/services/api.service';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';

@Component({
  selector: 'app-website-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    LucideDynamicIcon,
    BadgeComponent
  ],
  templateUrl: './website-detail.component.html'
})
export class WebsiteDetailComponent implements OnInit {
  website: any = null;
  placements: any[] = [];
  analytics: any = null;
  daily: any[] = [];

  today = new Date().toISOString().slice(0, 10);
  monthAgo = new Date(Date.now() - 30 * 864e5).toISOString().slice(0, 10);

  placement = { campaignId: '', adCreativeId: '' };
  showForm = false;
  formError = '';

  private route = inject(ActivatedRoute);
  private location = inject(Location);
  private api = inject(ApiService);

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadWebsite(id);
    }
  }

  loadWebsite(id: string) {
    this.api.publisher.getWebsite(id).subscribe({
      next: (res) => {
        this.website = res;
      }
    });

    this.api.publisher.getPlacements(id).subscribe({
      next: (res) => {
        this.placements = res;
      }
    });

    this.api.publisher.getAnalytics(id, this.monthAgo, this.today).subscribe({
      next: (res) => {
        this.analytics = res;
      },
      error: () => {}
    });

    this.api.publisher.getDailyAnalytics(id, this.monthAgo, this.today).subscribe({
      next: (res) => {
        this.daily = res;
      },
      error: () => {}
    });
  }

  goBack() {
    this.location.back();
  }

  createPlacement(e: Event) {
    e.preventDefault();
    if (!this.website?.id) return;
    this.formError = '';

    const payload: any = {
      websiteId: Number(this.website.id),
      campaignId: Number(this.placement.campaignId)
    };

    if (this.placement.adCreativeId) {
      payload.adCreativeId = Number(this.placement.adCreativeId);
    }

    this.api.publisher.createPlacement(payload).subscribe({
      next: () => {
        this.api.publisher.getPlacements(this.website.id).subscribe(res => {
          this.placements = res;
        });
        this.placement = { campaignId: '', adCreativeId: '' };
        this.showForm = false;
      },
      error: (err) => {
        this.formError = err.error?.error || 'Failed to create placement';
      }
    });
  }

  toggle(pid: any) {
    if (!this.website?.id) return;
    this.api.publisher.togglePlacement(pid).subscribe({
      next: () => {
        this.api.publisher.getPlacements(this.website.id).subscribe(res => {
          this.placements = res;
        });
      }
    });
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

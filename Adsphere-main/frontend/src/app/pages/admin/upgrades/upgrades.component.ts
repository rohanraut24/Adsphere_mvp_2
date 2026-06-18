import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideDynamicIcon } from '@lucide/angular';
import { ApiService } from '../../../core/services/api.service';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';

@Component({
  selector: 'app-admin-upgrades',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    LucideDynamicIcon,
    BadgeComponent
  ],
  templateUrl: './upgrades.component.html'
})
export class UpgradesComponent implements OnInit {
  requests: any[] = [];
  notes: Record<string, string> = {};
  loading = true;

  private api = inject(ApiService);

  ngOnInit() {
    this.load();
  }

  load() {
    this.api.admin.getPendingUpgrades().subscribe({
      next: (res) => {
        this.requests = res;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  review(id: any, decision: string) {
    const reviewNote = this.notes[id] || '';
    this.api.admin.reviewUpgrade(id, { decision, reviewNote }).subscribe({
      next: () => {
        this.load();
      }
    });
  }
}

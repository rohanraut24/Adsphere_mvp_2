import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideDynamicIcon } from '@lucide/angular';
import { ApiService } from '../../../core/services/api.service';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-pending-websites',
  standalone: true,
  imports: [
    CommonModule,
    LucideDynamicIcon,
    BadgeComponent
  ],
  templateUrl: './pending-websites.component.html'
})
export class PendingWebsitesComponent implements OnInit {
  websites: any[] = [];
  loading = true;

  private api = inject(ApiService);

  ngOnInit() {
    this.load();
  }

  load() {
    this.api.network.getPendingWebsites().subscribe({
      next: (res) => {
        this.websites = res;
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

  get approveWebsiteFn() { return this.api.network.approveWebsite.bind(this.api.network); }
  get rejectWebsiteFn() { return this.api.network.rejectWebsite.bind(this.api.network); }
}

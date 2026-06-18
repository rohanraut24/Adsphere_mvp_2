import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LucideDynamicIcon } from '@lucide/angular';
import { ApiService } from '../../../core/services/api.service';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';

@Component({
  selector: 'app-website-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    LucideDynamicIcon,
    BadgeComponent
  ],
  templateUrl: './website-list.component.html'
})
export class WebsiteListComponent implements OnInit {
  websites: any[] = [];
  search = '';

  private api = inject(ApiService);

  ngOnInit() {
    this.load();
  }

  load() {
    this.api.publisher.getWebsites().subscribe({
      next: (res) => {
        this.websites = res;
      }
    });
  }

  get visibleWebsites(): any[] {
    return this.websites.filter(w =>
      w.name.toLowerCase().includes(this.search.toLowerCase()) ||
      w.url.toLowerCase().includes(this.search.toLowerCase())
    );
  }

  deleteWebsite(id: any) {
    this.api.publisher.deleteWebsite(id).subscribe({
      next: () => {
        this.load();
      }
    });
  }
}

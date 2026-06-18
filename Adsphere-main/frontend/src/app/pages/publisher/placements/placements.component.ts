import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { forkJoin, Observable, of } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { LucideDynamicIcon } from '@lucide/angular';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-placements',
  standalone: true,
  imports: [
    CommonModule,
    LucideDynamicIcon
  ],
  templateUrl: './placements.component.html'
})
export class PlacementsComponent implements OnInit {
  placements: any[] = [];
  websites: any[] = [];
  loading = true;

  private api = inject(ApiService);

  ngOnInit() {
    this.load();
  }

  load() {
    this.api.publisher.getWebsites().pipe(
      switchMap((wRes: any[]) => {
        this.websites = wRes;
        if (wRes.length === 0) {
          return of([]);
        }
        const placementRequests = wRes.map(w =>
          this.api.publisher.getPlacements(w.id).pipe(
            map((pRes: any[]) =>
              pRes.map(p => ({ ...p, _websiteName: w.name }))
            )
          )
        );
        return forkJoin(placementRequests);
      })
    ).subscribe({
      next: (allPlacements: any[][]) => {
        this.placements = allPlacements.flat();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  toggle(pid: any) {
    this.api.publisher.togglePlacement(pid).subscribe({
      next: () => {
        this.load();
      }
    });
  }
}

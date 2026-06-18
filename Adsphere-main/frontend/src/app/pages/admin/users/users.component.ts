import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideDynamicIcon } from '@lucide/angular';
import { ApiService } from '../../../core/services/api.service';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    LucideDynamicIcon,
    BadgeComponent
  ],
  templateUrl: './users.component.html'
})
export class UsersComponent implements OnInit {
  users: any[] = [];
  role = 'ALL';
  search = '';
  loading = true;

  ROLES = ['ALL', 'SUPER_ADMIN', 'NETWORK_ADMIN', 'PUBLISHER', 'ADVERTISER'];

  private api = inject(ApiService);

  ngOnInit() {
    this.load(this.role);
  }

  load(r: string) {
    this.loading = true;
    const req$ = r === 'ALL' ? this.api.admin.getUsers() : this.api.admin.getUsersByRole(r);
    req$.subscribe({
      next: (res) => {
        this.users = res;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  onRoleChange(r: string) {
    this.role = r;
    this.load(r);
  }

  get visibleUsers(): any[] {
    return this.users.filter(u =>
      u.fullName?.toLowerCase().includes(this.search.toLowerCase()) ||
      u.email?.toLowerCase().includes(this.search.toLowerCase())
    );
  }

  getInitials(name: string): string {
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) : '?';
  }

  action(apiFn: (id: any) => Observable<any>, id: any) {
    apiFn(id).subscribe({
      next: () => {
        this.load(this.role);
      }
    });
  }

  get suspendUserFn() { return this.api.admin.suspendUser.bind(this.api.admin); }
  get activateUserFn() { return this.api.admin.activateUser.bind(this.api.admin); }
}

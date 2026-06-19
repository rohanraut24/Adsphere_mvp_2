import { Component, inject, OnInit } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { LucideDynamicIcon } from '@lucide/angular';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-website-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    LucideDynamicIcon
  ],
  templateUrl: './website-form.component.html'
})
export class WebsiteFormComponent implements OnInit {
  form!: FormGroup;
  isEdit = false;
  id: string | null = null;
  isSubmitting = false;
  errorMsg = '';

  CATEGORIES = ['Technology', 'News', 'Entertainment', 'Sports', 'Finance', 'Health', 'Education', 'Lifestyle', 'Travel', 'Food', 'Other'];

  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private location = inject(Location);

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id');
    this.isEdit = !!this.id;

    // URL regex pattern updated to allow localhost and ports for local development
    const urlPattern = /^(https?:\/\/)?([\da-z\.-]+)(:\d+)?([\/\w \.-]*)*\/?$/;

    this.form = this.fb.group({
      name: ['', [Validators.required]],
      url: ['', [Validators.required, Validators.pattern(urlPattern)]],
      category: ['', [Validators.required]]
    });

    if (this.isEdit && this.id) {
      this.api.publisher.getWebsite(this.id).subscribe({
        next: (res) => {
          this.form.patchValue(res);
        }
      });
    }
  }

  goBack() {
    this.location.back();
  }

  onSubmit() {
    if (this.form.invalid) return;
    this.isSubmitting = true;
    this.errorMsg = '';

    const action$ = this.isEdit && this.id
      ? this.api.publisher.updateWebsite(this.id, this.form.value)
      : this.api.publisher.createWebsite(this.form.value);

    action$.subscribe({
      next: () => {
        this.isSubmitting = false;
        this.router.navigate(['/publisher/websites']);
      },
      error: (err) => {
        this.errorMsg = err.error?.error || 'Failed to save website';
        this.isSubmitting = false;
      }
    });
  }
}

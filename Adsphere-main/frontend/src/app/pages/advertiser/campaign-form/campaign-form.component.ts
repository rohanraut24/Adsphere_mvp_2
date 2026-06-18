import { Component, inject, OnInit } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { LucideDynamicIcon } from '@lucide/angular';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-campaign-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    LucideDynamicIcon
  ],
  templateUrl: './campaign-form.component.html'
})
export class CampaignFormComponent implements OnInit {
  form!: FormGroup;
  isEdit = false;
  id: string | null = null;
  isSubmitting = false;
  errorMsg = '';

  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private location = inject(Location);

  ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id');
    this.isEdit = !!this.id;

    this.form = this.fb.group({
      name: ['', [Validators.required]],
      description: [''],
      budget: ['', [Validators.required, Validators.min(0.01)]],
      cpcBid: ['', [Validators.required, Validators.min(0.001)]],
      startDate: ['', [Validators.required]],
      endDate: ['', [Validators.required]]
    });

    if (this.isEdit && this.id) {
      this.api.advertiser.getCampaign(this.id).subscribe({
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
      ? this.api.advertiser.updateCampaign(this.id, this.form.value)
      : this.api.advertiser.createCampaign(this.form.value);

    action$.subscribe({
      next: () => {
        this.isSubmitting = false;
        this.router.navigate(['/advertiser/campaigns']);
      },
      error: (err) => {
        this.errorMsg = err.error?.error || 'Failed to save campaign';
        this.isSubmitting = false;
      }
    });
  }
}

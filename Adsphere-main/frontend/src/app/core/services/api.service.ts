import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private http = inject(HttpClient);
  private prefix = '/api';

  auth = {
    login: (data: any): Observable<any> => this.http.post(`${this.prefix}/auth/login`, data),
    register: (data: any): Observable<any> => this.http.post(`${this.prefix}/auth/register`, data),
  };

  advertiser = {
    getCampaigns: (): Observable<any> => this.http.get(`${this.prefix}/advertiser/campaigns`),
    getCampaign: (id: any): Observable<any> => this.http.get(`${this.prefix}/advertiser/campaigns/${id}`),
    createCampaign: (data: any): Observable<any> => this.http.post(`${this.prefix}/advertiser/campaigns`, data),
    updateCampaign: (id: any, data: any): Observable<any> => this.http.put(`${this.prefix}/advertiser/campaigns/${id}`, data),
    deleteCampaign: (id: any): Observable<any> => this.http.delete(`${this.prefix}/advertiser/campaigns/${id}`),
    submitCampaign: (id: any): Observable<any> => this.http.put(`${this.prefix}/advertiser/campaigns/${id}/submit`, {}),
    pauseCampaign: (id: any): Observable<any> => this.http.put(`${this.prefix}/advertiser/campaigns/${id}/pause`, {}),
    resumeCampaign: (id: any): Observable<any> => this.http.put(`${this.prefix}/advertiser/campaigns/${id}/resume`, {}),
    getCreatives: (id: any): Observable<any> => this.http.get(`${this.prefix}/advertiser/campaigns/${id}/creatives`),
    addCreative: (id: any, data: any): Observable<any> => this.http.post(`${this.prefix}/advertiser/campaigns/${id}/creatives`, data),
    deleteCreative: (cid: any, rid: any): Observable<any> => this.http.delete(`${this.prefix}/advertiser/campaigns/${cid}/creatives/${rid}`),
    getPlacements: (id: any): Observable<any> => this.http.get(`${this.prefix}/advertiser/campaigns/${id}/placements`),
    getSpend: (): Observable<any> => this.http.get(`${this.prefix}/advertiser/spend`),
    getTransactions: (): Observable<any> => this.http.get(`${this.prefix}/advertiser/transactions`),
    getAnalytics: (id: any, from: string, to: string): Observable<any> => this.http.get(`${this.prefix}/advertiser/campaigns/${id}/analytics`, { params: { from, to } }),
    getDailyAnalytics: (id: any, from: string, to: string): Observable<any> => this.http.get(`${this.prefix}/advertiser/campaigns/${id}/analytics/daily`, { params: { from, to } }),
    getGlobalDailyAnalytics: (from: string, to: string): Observable<any> => this.http.get(`${this.prefix}/advertiser/analytics/daily`, { params: { from, to } }),
  };

  publisher = {
    getWebsites: (): Observable<any> => this.http.get(`${this.prefix}/publisher/websites`),
    getWebsite: (id: any): Observable<any> => this.http.get(`${this.prefix}/publisher/websites/${id}`),
    createWebsite: (data: any): Observable<any> => this.http.post(`${this.prefix}/publisher/websites`, data),
    updateWebsite: (id: any, data: any): Observable<any> => this.http.put(`${this.prefix}/publisher/websites/${id}`, data),
    deleteWebsite: (id: any): Observable<any> => this.http.delete(`${this.prefix}/publisher/websites/${id}`),
    getPlacements: (websiteId: any): Observable<any> => this.http.get(`${this.prefix}/publisher/websites/${websiteId}/placements`),
    createPlacement: (data: any): Observable<any> => this.http.post(`${this.prefix}/publisher/placements`, data),
    togglePlacement: (id: any): Observable<any> => this.http.put(`${this.prefix}/publisher/placements/${id}/toggle`, {}),
    getEarnings: (): Observable<any> => this.http.get(`${this.prefix}/publisher/earnings`),
    getTransactions: (): Observable<any> => this.http.get(`${this.prefix}/publisher/transactions`),
    getAnalytics: (id: any, from: string, to: string): Observable<any> => this.http.get(`${this.prefix}/publisher/websites/${id}/analytics`, { params: { from, to } }),
    getDailyAnalytics: (id: any, from: string, to: string): Observable<any> => this.http.get(`${this.prefix}/publisher/websites/${id}/analytics/daily`, { params: { from, to } }),
    getGlobalDailyAnalytics: (from: string, to: string): Observable<any> => this.http.get(`${this.prefix}/publisher/analytics/daily`, { params: { from, to } }),
    getUpgradeRequests: (): Observable<any> => this.http.get(`${this.prefix}/publisher/upgrade-requests`),
    createUpgradeRequest: (data: any): Observable<any> => this.http.post(`${this.prefix}/publisher/upgrade-requests`, data),
  };

  network = {
    getPendingWebsites: (): Observable<any> => this.http.get(`${this.prefix}/network/websites/pending`),
    approveWebsite: (id: any): Observable<any> => this.http.put(`${this.prefix}/network/websites/${id}/approve`, {}),
    rejectWebsite: (id: any): Observable<any> => this.http.put(`${this.prefix}/network/websites/${id}/reject`, {}),
    getPendingCampaigns: (): Observable<any> => this.http.get(`${this.prefix}/network/campaigns/pending`),
    approveCampaign: (id: any): Observable<any> => this.http.put(`${this.prefix}/network/campaigns/${id}/approve`, {}),
    rejectCampaign: (id: any): Observable<any> => this.http.put(`${this.prefix}/network/campaigns/${id}/reject`, {}),
  };

  admin = {
    getStats: (): Observable<any> => this.http.get(`${this.prefix}/admin/stats`),
    getUsers: (): Observable<any> => this.http.get(`${this.prefix}/admin/users`),
    getUsersByRole: (role: string): Observable<any> => this.http.get(`${this.prefix}/admin/users/role/${role}`),
    suspendUser: (id: any): Observable<any> => this.http.put(`${this.prefix}/admin/users/${id}/suspend`, {}),
    activateUser: (id: any): Observable<any> => this.http.put(`${this.prefix}/admin/users/${id}/activate`, {}),
    getPendingUpgrades: (): Observable<any> => this.http.get(`${this.prefix}/admin/upgrade-requests/pending`),
    reviewUpgrade: (id: any, data: any): Observable<any> => this.http.put(`${this.prefix}/admin/upgrade-requests/${id}/review`, data),
  };
}

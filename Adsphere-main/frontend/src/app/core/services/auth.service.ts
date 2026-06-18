import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface User {
  token: string;
  role: string;
  email: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private userSubject = new BehaviorSubject<User | null>(this.getInitialUser());
  user$ = this.userSubject.asObservable();

  get currentUser(): User | null {
    return this.userSubject.value;
  }

  private getInitialUser(): User | null {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    const email = localStorage.getItem('email');
    return token && role && email ? { token, role, email } : null;
  }

  login(userData: User) {
    localStorage.setItem('token', userData.token);
    localStorage.setItem('role', userData.role);
    localStorage.setItem('email', userData.email);
    this.userSubject.next(userData);
  }

  logout() {
    localStorage.clear();
    this.userSubject.next(null);
  }
}

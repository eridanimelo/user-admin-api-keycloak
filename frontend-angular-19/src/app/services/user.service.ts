import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserDTO {
  username?: string;
  firstName?: string;
  lastName?: string;
  email: string;
  password: string;
  enabled?: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly apiUrl = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient) { }

  createUser(user: UserDTO): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/create`, user);
  }

  assignRole(userId: string, roleName: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/users/${userId}/roles/add?roleName=${roleName}`, {});
  }

  removeRole(userId: string, roleName: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/users/${userId}/roles/remove?roleName=${roleName}`, {});
  }

  resetPassword(user: UserDTO): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/reset-password`, user);
  }

  deleteUser(email: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/delete?email=${email}`);
  }

  disableUser(email: string): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/disable?email=${email}`, {});
  }

  enableUser(email: string): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/enable?email=${email}`, {});
  }

  listAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/list`);
  }

  listAllRoles(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/roles`);
  }
}

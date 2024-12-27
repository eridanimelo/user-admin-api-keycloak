import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, ValidationErrors, ValidatorFn, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDialog } from '@angular/material/dialog';

import { KeycloakService } from 'keycloak-angular';
import { ToastrService } from 'ngx-toastr';

import { UserRepresentation, UserService } from '../../services/user.service';

import { AddUserDialogComponent } from './add-user-dialog/add-user-dialog.component';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog.component';
import { ResetPasswordDialogComponent } from './reset-password-dialog/reset-password-dialog.component';
import { AssignRoleDialogComponent } from './assign-role-dialog/assign-role-dialog.component';

export const passwordMatchValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  return password === confirmPassword ? null : { mismatch: true };
};

@Component({
  selector: 'app-user-management',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, MatInputModule,
    MatButtonModule,
    MatTableModule,
    MatIconModule,
    MatCardModule,
    MatToolbarModule,
    MatSlideToggleModule],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.css'
})
export class UserManagementComponent implements OnInit {
  userForm: FormGroup;
  users: any[] = [];

  constructor(
    private dialog: MatDialog,
    private toastr: ToastrService,
    private fb: FormBuilder,
    private userService: UserService,
    private keycloakService: KeycloakService) {
    this.userForm = this.fb.nonNullable.group(
      {
        username: ['', Validators.required],
        firstName: ['', Validators.required],
        lastName: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        password: ['', Validators.required],
        confirmPassword: ['', Validators.required],
      },
      { validators: [passwordMatchValidator] }
    );
  }

  async ngOnInit() {
    const token = await this.keycloakService.getToken();
    console.log(token);
    this.loadUsers();
  }

  openAddUserDialog() {
    const dialogRef = this.dialog.open(AddUserDialogComponent, {
      width: '420px'
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        // this.users.push(result);
        this.loadUsers();
      }
    });
  }

  loadUsers(): void {
    this.userService.listAllUsers().subscribe({
      next: (users) => {

        this.users = users
        console.log(this.users);
      },
      error: (err) => {
        console.error(err);
        this.toastr.error('Error loading users.', 'Toastr fun!');
      },
    });
  }



  resetPassword(user: UserRepresentation): void {
    const dialogRef = this.dialog.open(ResetPasswordDialogComponent, {
      width: '300px',
      data: { user },
    });

    dialogRef.afterClosed().subscribe((newPassword) => {
      if (newPassword) {

      }
    });
  }

  deleteUser(user: UserRepresentation): void {

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '300px',
      data: { message: 'Are you sure you want to delete this user?' },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.userService.deleteUser(user.id!).subscribe({
          next: () => {

            this.toastr.success('User deleted successfully!', 'Toastr fun!');
            this.loadUsers();
          },
          error: (err) => {
            console.error(err);
            this.toastr.error('Error deleting user.', 'Toastr fun!');
          },
        });
      }
    });
  }

  onToggleChangeDisable(user: UserRepresentation): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '300px',
      data: { message: `Are you sure you want to ${!user.enabled ? 'disable' : 'enable'} this user?` },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        if (!user.enabled) {
          this.userService.disableUser(user.id!).subscribe({
            next: () => {
              this.toastr.success(`User ${!user.enabled ? 'disable' : 'enable'}  successfully!`, 'Toastr fun!');
              // this.loadUsers();
            },
            error: (err) => {
              console.error(err);
              this.toastr.error('Error disable user.', 'Toastr fun!');
            },
          });
        } else {
          this.userService.enableUser(user.id!).subscribe({
            next: () => {
              this.toastr.success(`User ${!user.enabled ? 'disable' : 'enable'}  successfully!`, 'Toastr fun!');
              // this.loadUsers();
            },
            error: (err) => {
              console.error(err);
              this.toastr.error('Error enable user.', 'Toastr fun!');
            },
          });
        }

      } else {
        user.enabled = !user.enabled;
      }
    });
  }

  openAssignRoleDialog(user: any): void {
    const dialogRef = this.dialog.open(AssignRoleDialogComponent, {
      width: '400px',
      data: { userId: user.id },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.toastr.success('Role assigned successfully!', 'Success');
        this.loadUsers();
      }
    });
  }

  removeRole(user: any, roleName: string): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '300px',
      data: { message: `Are you sure you want to remove the role "${roleName}" from ${user.username}?` },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.userService.removeRole(user.id, roleName).subscribe({
          next: () => {
            this.toastr.success(`Role "${roleName}" removed successfully!`, 'Success');
            this.loadUsers();
          },
          error: (err) => {
            console.error(err);
            this.toastr.error(`Failed to remove role "${roleName}".`, 'Error');
          },
        });
      }
    });
  }

}
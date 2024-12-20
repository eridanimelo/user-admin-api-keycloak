import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { UserDTO, UserService } from '../../../services/user.service';
import { passwordMatchValidator } from '../user-management.component';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-reset-password-dialog',
  imports: [
    MatDialogModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './reset-password-dialog.component.html',
  styleUrl: './reset-password-dialog.component.css'
})
export class ResetPasswordDialogComponent {

  form: FormGroup;
  email!: string;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private toastr: ToastrService,
    public dialogRef: MatDialogRef<ResetPasswordDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { email: string }
  ) {
    this.email = data['email'];
    this.form = this.fb.nonNullable.group(
      {
        password: ['', Validators.required],
        confirmPassword: ['', Validators.required],
      },
      { validators: passwordMatchValidator }
    );
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSubmit(): void {
    if (this.form.valid) {
      const user: UserDTO = {
        email: this.email, password: this.form.get('password')?.value
      };
      this.userService.resetPassword(user).subscribe({
        next: () => {
          this.dialogRef.close(true);
        },
        error: (err) => {
          console.error(err);
          this.toastr.error('Error resetting password.', 'Toastr fun!');
        },
      });
    }
  }
}

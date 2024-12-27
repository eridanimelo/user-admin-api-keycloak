import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { UserRequestDTO, UserRepresentation, UserService } from '../../../services/user.service';
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
  styleUrls: ['./reset-password-dialog.component.css'],
})
export class ResetPasswordDialogComponent {

  form: FormGroup;
  user!: UserRepresentation;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private toastr: ToastrService,
    public dialogRef: MatDialogRef<ResetPasswordDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { user: UserRepresentation }
  ) {
    this.user = data['user'];

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

      const userRequest: UserRequestDTO = {
        user: this.user,
        password: this.form.get('password')?.value,
      };

      this.userService.resetPassword(userRequest).subscribe({
        next: () => {
          this.dialogRef.close(true);
          this.toastr.success('Password reset successfully.', 'Success!');
        },
        error: (err) => {
          console.error(err);
          this.toastr.error('Error resetting password.', 'Error');
        },
      });
    }
  }
}

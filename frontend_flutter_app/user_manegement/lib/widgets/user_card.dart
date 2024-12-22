import 'package:flutter/material.dart';

class UserCard extends StatelessWidget {
  final Map<String, dynamic> user;
  final VoidCallback? onDelete;
  final VoidCallback? onDisable;
  final VoidCallback? onEnable;
  final VoidCallback? onChangePassword;

  const UserCard({
    super.key,
    required this.user,
    this.onDelete,
    this.onDisable,
    this.onEnable,
    this.onChangePassword,
  });

  @override
  Widget build(BuildContext context) {
    final isEnabled = user['enabled'] ?? true; // Supondo que o estado de habilitação esteja em `user['enabled']`.

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: ListTile(
        leading: CircleAvatar(
          child: Text(user['username']?.substring(0, 1) ?? '?'),
        ),
        title: Text(user['username'] ?? 'No Name'),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(user['email'] ?? 'No Email'),
            const SizedBox(height: 4),
            Text(
              isEnabled ? 'Status: Enabled' : 'Status: Disabled',
              style: TextStyle(
                color: isEnabled ? Colors.green : Colors.red,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ),
        trailing: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            IconButton(
              icon: Icon(isEnabled ? Icons.block : Icons.check),
              onPressed: isEnabled ? onDisable : onEnable,
              tooltip: isEnabled ? 'Disable User' : 'Enable User',
            ),
            IconButton(
              icon: const Icon(Icons.lock_reset),
              onPressed: onChangePassword,
              tooltip: 'Change Password',
            ),
            IconButton(
              icon: const Icon(Icons.delete),
              onPressed: onDelete,
              tooltip: 'Delete User',
            ),
          ],
        ),
      ),
    );
  }
}

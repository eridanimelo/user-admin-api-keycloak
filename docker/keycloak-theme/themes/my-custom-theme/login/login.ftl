<!-- keycloak-themes/my-custom-theme/login/login.ftl -->
<!DOCTYPE html>
<html>
<head>
    <link rel="stylesheet" href="css/login.css">
</head>
<body>
    <div class="login-container">
        
        <form id="kc-form-login" onsubmit="loginDisabledButton()">
            <input type="text" name="username" placeholder="Username">
            <input type="password" name="password" placeholder="Password">
            <input type="submit" value="Login">
        </form>
    </div>
</body>
</html>

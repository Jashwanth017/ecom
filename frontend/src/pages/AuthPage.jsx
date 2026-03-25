import { useEffect, useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

const roleOptions = [
  { role: "buyer", label: "Buyer", loginPath: "/buyer/login", registerPath: "/buyer/register" },
  { role: "seller", label: "Seller", loginPath: "/seller/login", registerPath: "/seller/register" },
  { role: "admin", label: "Admin", loginPath: "/admin/login" }
];

const initialFormState = {
  fullName: "",
  storeName: "",
  storeDescription: "",
  email: "",
  password: "",
  phone: ""
};

function AuthPage({ role, mode }) {
  const navigate = useNavigate();
  const { isAuthenticated, status, user, login, registerBuyer, registerSeller } = useAuth();
  const activeRole = roleOptions.find((option) => option.role === role) ?? roleOptions[0];
  const canRegister = activeRole.role !== "admin";
  const pageTitle = `${capitalize(activeRole.role)} ${mode === "register" ? "Register" : "Login"}`;
  const submitLabel = mode === "register" ? "Create Account" : "Continue";
  const [formState, setFormState] = useState(initialFormState);
  const [feedback, setFeedback] = useState({ error: "", success: "" });

  useEffect(() => {
    setFormState(initialFormState);
    setFeedback({ error: "", success: "" });
  }, [role, mode]);

  useEffect(() => {
    if (isAuthenticated && user?.redirectTo) {
      navigate(resolvePostLoginPath(user.role, user.redirectTo), { replace: true });
    }
  }, [isAuthenticated, navigate, user]);

  function updateField(field) {
    return (event) => {
      setFormState((previous) => ({
        ...previous,
        [field]: event.target.value
      }));
    };
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setFeedback({ error: "", success: "" });

    try {
      if (mode === "login") {
        const response = await login({
          email: formState.email,
          password: formState.password,
          role: activeRole.role.toUpperCase()
        });

        navigate(resolvePostLoginPath(response.role, response.redirectTo), { replace: true });
        return;
      }

      if (activeRole.role === "buyer") {
        const response = await registerBuyer({
          email: formState.email,
          password: formState.password,
          fullName: formState.fullName,
          phone: formState.phone
        });

        setFeedback({
          error: "",
          success: response.message || "Buyer account created. You can login now."
        });
        navigate("/buyer/login", { replace: true });
        return;
      }

      const response = await registerSeller({
        email: formState.email,
        password: formState.password,
        storeName: formState.storeName,
        storeDescription: formState.storeDescription
      });

      setFeedback({
        error: "",
        success: response.message || "Seller account created. You can login now."
      });
      navigate("/seller/login", { replace: true });
    } catch (error) {
      setFeedback({
        error: error.message || "Unable to continue right now.",
        success: ""
      });
    }
  }

  return (
    <section className="auth-card">
      <aside className="auth-sidebar">
        <div className="auth-role-list">
          {roleOptions.map((option) => (
            <NavLink
              key={option.role}
              to={option.loginPath}
              className={({ isActive }) =>
                `auth-role-link ${isActive || option.role === role ? "active" : ""}`
              }
            >
              <strong>{option.label}</strong>
            </NavLink>
          ))}
        </div>
      </aside>

      <div className="auth-form-panel">
        <div className="auth-form-header">
          <h3>{pageTitle}</h3>
        </div>

        <div className="mode-toggle">
          <NavLink to={activeRole.loginPath} className={`mode-link ${mode === "login" ? "active" : ""}`}>
            Login
          </NavLink>
          {canRegister ? (
            <NavLink
              to={activeRole.registerPath}
              className={`mode-link ${mode === "register" ? "active" : ""}`}
            >
              Register
            </NavLink>
          ) : null}
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          {mode === "register" && activeRole.role !== "admin" ? (
            <>
              {activeRole.role === "buyer" ? (
                <label>
                  Full Name
                  <input
                    type="text"
                    placeholder="Enter your full name"
                    value={formState.fullName}
                    onChange={updateField("fullName")}
                  />
                </label>
              ) : null}
              {activeRole.role === "seller" ? (
                <label>
                  Store Name
                  <input
                    type="text"
                    placeholder="Enter your store name"
                    value={formState.storeName}
                    onChange={updateField("storeName")}
                  />
                </label>
              ) : null}
            </>
          ) : null}

          <label>
            Email
            <input
              type="email"
              placeholder="Enter your email"
              value={formState.email}
              onChange={updateField("email")}
            />
          </label>

          <label>
            Password
            <input
              type="password"
              placeholder="Enter your password"
              value={formState.password}
              onChange={updateField("password")}
            />
          </label>

          {mode === "register" && activeRole.role === "buyer" ? (
            <label>
              Phone
              <input
                type="tel"
                placeholder="Enter your phone number"
                value={formState.phone}
                onChange={updateField("phone")}
              />
            </label>
          ) : null}

          {mode === "register" && activeRole.role === "seller" ? (
            <label>
              Store Description
              <textarea
                rows="4"
                placeholder="Tell users about your store"
                value={formState.storeDescription}
                onChange={updateField("storeDescription")}
              />
            </label>
          ) : null}

          {feedback.error ? <p className="form-feedback form-feedback-error">{feedback.error}</p> : null}
          {feedback.success ? <p className="form-feedback form-feedback-success">{feedback.success}</p> : null}

          <button type="submit" className="button button-primary" disabled={status === "loading"}>
            {submitLabel}
          </button>
        </form>

        <div className="auth-help-row">
          <a href="/">Back to home</a>
        </div>
      </div>
    </section>
  );
}

function capitalize(value) {
  return value.charAt(0).toUpperCase() + value.slice(1);
}

function resolvePostLoginPath(role, redirectTo) {
  if (role === "SELLER" || role === "ADMIN") {
    return redirectTo;
  }
  return "/";
}

export default AuthPage;

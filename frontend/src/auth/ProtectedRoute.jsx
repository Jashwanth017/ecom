import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "./AuthContext";

function ProtectedRoute({ allowedRoles }) {
  const { isAuthenticated, user, status } = useAuth();
  const location = useLocation();

  if (status === "bootstrapping") {
    return <div className="route-loading-state">Checking your session...</div>;
  }

  if (!isAuthenticated || !user) {
    return <Navigate to={resolveLoginPath(allowedRoles)} replace state={{ from: location }} />;
  }

  if (!allowedRoles.includes(user.role)) {
    return <Navigate to={resolveDefaultDashboard(user.role)} replace />;
  }

  return <Outlet />;
}

function resolveLoginPath(allowedRoles) {
  if (allowedRoles.includes("ADMIN")) {
    return "/admin/login";
  }
  if (allowedRoles.includes("SELLER")) {
    return "/seller/login";
  }
  return "/buyer/login";
}

function resolveDefaultDashboard(role) {
  if (role === "ADMIN") {
    return "/admin/dashboard";
  }
  if (role === "SELLER") {
    return "/seller/dashboard";
  }
  return "/";
}

export default ProtectedRoute;

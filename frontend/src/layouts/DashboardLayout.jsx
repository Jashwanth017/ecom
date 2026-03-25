import { NavLink, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

const sectionLinks = {
  "/seller": [
    { to: "/seller/dashboard", label: "Dashboard" },
    { to: "/seller/products", label: "Products" },
    { to: "/seller/categories", label: "Categories" },
    { to: "/seller/orders", label: "Orders" },
    { to: "/seller/profile", label: "Profile" }
  ],
  "/admin": [
    { to: "/admin/dashboard", label: "Dashboard" },
    { to: "/admin/users", label: "Users" },
    { to: "/admin/seller-approvals", label: "Approvals" },
    { to: "/admin/categories", label: "Categories" }
  ]
};

function DashboardLayout({ areaName }) {
  const location = useLocation();
  const { user, logout } = useAuth();
  const areaRoot = Object.keys(sectionLinks).find((prefix) => location.pathname.startsWith(prefix)) || "/seller";
  const links = sectionLinks[areaRoot];

  return (
    <div className="app-shell dashboard-shell">
      <aside className="sidebar dashboard-sidebar">
        <div className="dashboard-sidebar-head">
          <h2>{areaName}</h2>
          <span>{user?.email}</span>
        </div>
        <nav className="sidebar-nav">
          {links.map((link) => (
            <NavLink key={link.to} to={link.to} className="nav-link dashboard-nav-link">
              {link.label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-footer">
          <button type="button" className="button button-secondary sidebar-logout" onClick={logout}>
            Logout
          </button>
        </div>
      </aside>
      <main className="page-frame dashboard-page-frame">
        <Outlet />
      </main>
    </div>
  );
}

export default DashboardLayout;

import { Link, NavLink, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

const guestLinks = [
  { to: "/", label: "Home" },
  { to: "/products", label: "Products" }
];

const quickActions = {
  BUYER: [
    { to: "/buyer/cart", label: "🛒", title: "Cart" },
    { to: "/buyer/orders", label: "📦", title: "Orders" },
    { to: "/buyer/profile", label: "👤", title: "Profile" }
  ],
  SELLER: [
    { to: "/seller/orders", label: "📦", title: "Orders" },
    { to: "/seller/profile", label: "🏪", title: "Profile" },
    { to: "/seller/dashboard", label: "📊", title: "Dashboard" }
  ],
  ADMIN: [
    { to: "/admin/dashboard", label: "📊", title: "Dashboard" },
    { to: "/admin/users", label: "👥", title: "Users" },
    { to: "/admin/categories", label: "🗂️", title: "Categories" }
  ]
};

function PublicLayout() {
  const { isAuthenticated, logout, user } = useAuth();
  const location = useLocation();
  const showCategories = location.pathname === "/" || location.pathname === "/products";
  const actions = quickActions[user?.role] ?? [];

  return (
    <div className="app-shell public-shell">
      <div className="promo-bar">
        <span>Free delivery on first orders above $49</span>
        <span>New seller onboarding is open</span>
      </div>

      <header className="topbar ecommerce-topbar">
        <Link to="/" className="brand-block">
          <h1>e-market</h1>
        </Link>

        <div className="search-shell">
          <input type="text" placeholder="Search for shoes, gadgets, home decor..." />
        </div>

        <div className="header-actions">
          <nav className="nav-row">
            {guestLinks.map((link) => (
              <NavLink key={link.to} to={link.to} className="nav-link">
                {link.label}
              </NavLink>
            ))}
          </nav>

          {isAuthenticated ? (
            <div className="quick-action-row">
              {actions.map((action) => (
                <NavLink key={action.to} to={action.to} className="quick-action-link" title={action.title}>
                  {action.label}
                </NavLink>
              ))}
              <button
                type="button"
                className="quick-action-link quick-action-button"
                onClick={logout}
                title="Logout"
              >
                🚪
              </button>
            </div>
          ) : (
            <div className="quick-action-row">
              <NavLink to="/buyer/login" className="quick-action-link">Login</NavLink>
            </div>
          )}
        </div>
      </header>

      {showCategories ? (
        <div className="category-strip">
          {["Fashion", "Electronics", "Kitchen", "Books", "Beauty", "Sports", "Furniture"].map((item) => (
            <button key={item} type="button" className="category-pill">
              {item}
            </button>
          ))}
        </div>
      ) : null}

      <main className="page-frame">
        <Outlet />
      </main>
    </div>
  );
}

export default PublicLayout;

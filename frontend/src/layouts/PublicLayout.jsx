import { Link, NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { apiClient } from "../api/client";
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
  ]
};

function PublicLayout() {
  const { isAuthenticated, logout, user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const showCategories = location.pathname === "/" || location.pathname === "/products";
  const actions = quickActions[user?.role] ?? [];
  const [categories, setCategories] = useState([]);
  const [searchValue, setSearchValue] = useState("");

  useEffect(() => {
    if (!showCategories) {
      return;
    }

    async function loadCategories() {
      try {
        const data = await apiClient.getCategories();
        setCategories(data ?? []);
      } catch {
        setCategories([]);
      }
    }

    loadCategories();
  }, [showCategories]);

  function handleSearchSubmit(event) {
    event.preventDefault();
    const trimmedSearch = searchValue.trim();
    if (!trimmedSearch) {
      navigate("/products");
      return;
    }

    navigate(`/products?search=${encodeURIComponent(trimmedSearch)}`);
  }

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

        <form className="search-shell" onSubmit={handleSearchSubmit}>
          <input
            type="text"
            placeholder="Search for shoes, gadgets, home decor..."
            value={searchValue}
            onChange={(event) => setSearchValue(event.target.value)}
          />
        </form>

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
          {categories.length === 0 ? (
            <span className="category-pill category-pill-static">Catalog categories will appear here.</span>
          ) : (
            categories.map((item) => (
              <Link key={item.categoryId} to={`/products?categoryId=${item.categoryId}`} className="category-pill">
                {item.name}
              </Link>
            ))
          )}
        </div>
      ) : null}

      <main className="page-frame">
        <Outlet />
      </main>
    </div>
  );
}

export default PublicLayout;

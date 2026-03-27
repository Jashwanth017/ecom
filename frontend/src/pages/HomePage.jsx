import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiClient } from "../api/client";

function HomePage() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadHomeData() {
      setLoading(true);
      try {
        const [catalogProducts, catalogCategories] = await Promise.all([
          apiClient.getCatalogProducts(),
          apiClient.getCategories()
        ]);
        setProducts((catalogProducts ?? []).slice(0, 6));
        setCategories(catalogCategories ?? []);
        setError("");
      } catch (err) {
        setError(err.message || "Unable to load marketplace catalog.");
      } finally {
        setLoading(false);
      }
    }

    loadHomeData();
  }, []);

  return (
    <div className="home-stack">
      <section className="hero-card">
        <div className="hero-copy">
          <p className="eyebrow">Marketplace Home</p>
          <h2>Everything you need, from everyday buys to seller storefronts.</h2>
          <p>
            The storefront stays open by default. Buyers browse first, sellers list products later, and
            authentication starts only when the user chooses to sign in.
          </p>
          <div className="hero-actions">
            <Link to="/buyer/login" className="button button-primary">Login to Continue</Link>
            <Link to="/products" className="button button-secondary">Shop the Catalog</Link>
          </div>
          <div className="hero-metrics">
            <div>
              <strong>{loading ? "..." : products.length}</strong>
              <span>Featured live products</span>
            </div>
            <div>
              <strong>{loading ? "..." : categories.length}</strong>
              <span>Browseable categories</span>
            </div>
            <div>
              <strong>48h</strong>
              <span>Fast dispatch</span>
            </div>
          </div>
        </div>
        <div className="hero-panel deal-panel">
          <span>Festival Deal Window</span>
          <strong>Save up to 60% on trending electronics</strong>
          <p>Use this hero panel later for backend-driven banners, countdowns, and category offers.</p>
          <div className="deal-tickets">
            <span>Today’s Picks</span>
            <span>Top Rated</span>
            <span>Fast Moving</span>
          </div>
        </div>
      </section>

      <section className="feature-strip">
        <article className="feature-card">
          <span>Secure checkout</span>
          <strong>Stateless auth + protected account areas</strong>
        </article>
        <article className="feature-card">
          <span>Seller controls</span>
          <strong>Inventory, orders, and dashboard summaries</strong>
        </article>
        <article className="feature-card">
          <span>Admin moderation</span>
          <strong>Category, seller, and account management</strong>
        </article>
      </section>

      <section className="catalog-preview">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Trending Products</p>
            <h3>Live picks from the catalog</h3>
          </div>
          <Link to="/products" className="button button-secondary">Open Catalog</Link>
        </div>

        {error ? <p className="form-feedback form-feedback-error">{error}</p> : null}

        {loading ? (
          <div className="product-grid">
            {Array.from({ length: 3 }).map((_, index) => (
              <article key={index} className="product-card product-card-skeleton">
                <span className="product-category">Loading</span>
                <h4>Loading product...</h4>
                <p>Fetching live catalog data from the backend.</p>
              </article>
            ))}
          </div>
        ) : products.length === 0 ? (
          <div className="placeholder-card">
            <p className="eyebrow">Catalog Empty</p>
            <h2>No products are available yet.</h2>
            <p>Add products from a seller account and they will appear here automatically.</p>
          </div>
        ) : (
          <div className="product-grid">
            {products.map((product) => (
              <article key={product.productId} className="product-card">
                <span className="product-category">{product.categoryName}</span>
                <h4>{product.name}</h4>
                <p>Explore this live catalog item and view more details on the product page.</p>
                <div className="product-footer">
                  <div className="price-stack">
                    <strong>{formatCurrency(product.price)}</strong>
                  </div>
                  <Link to={`/products/${product.productId}`} className="text-link">Details</Link>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

function formatCurrency(value) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD"
  }).format(Number(value ?? 0));
}

export default HomePage;

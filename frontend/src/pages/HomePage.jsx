import { Link } from "react-router-dom";

const featuredProducts = [
  { id: 1, name: "Nova Wireless Headphones", category: "Electronics", price: "$129", oldPrice: "$169", blurb: "Noise-isolated sound with a 40-hour battery." },
  { id: 2, name: "Daily Move Sneakers", category: "Fashion", price: "$74", oldPrice: "$99", blurb: "Built for long city walks and everyday wear." },
  { id: 3, name: "Nordic Brew Kettle", category: "Kitchen", price: "$58", oldPrice: "$79", blurb: "Pour-over precision in a matte steel finish." },
  { id: 4, name: "Studio Table Lamp", category: "Home", price: "$42", oldPrice: "$56", blurb: "Warm glow for desks, nightstands, and corners." },
  { id: 5, name: "Pulse Smartwatch", category: "Electronics", price: "$149", oldPrice: "$189", blurb: "Fitness tracking, message sync, and clean AMOLED display." },
  { id: 6, name: "Linen Weekend Tote", category: "Fashion", price: "$61", oldPrice: "$82", blurb: "Structured carry-all with polished hardware details." }
];

function HomePage() {
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
              <strong>1.2k+</strong>
              <span>Products ready</span>
            </div>
            <div>
              <strong>250+</strong>
              <span>Verified sellers</span>
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
            <h3>Best-selling picks this week</h3>
          </div>
          <Link to="/products" className="button button-secondary">Open Catalog</Link>
        </div>

        <div className="product-grid">
          {featuredProducts.map((product) => (
            <article key={product.id} className="product-card">
              <span className="product-category">{product.category}</span>
              <h4>{product.name}</h4>
              <p>{product.blurb}</p>
              <div className="product-footer">
                <div className="price-stack">
                  <strong>{product.price}</strong>
                  <span>{product.oldPrice}</span>
                </div>
                <Link to={`/products/${product.id}`} className="text-link">Details</Link>
              </div>
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}

export default HomePage;

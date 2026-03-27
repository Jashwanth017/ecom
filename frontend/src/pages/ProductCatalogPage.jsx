import { useEffect, useState } from "react";
import { Link, useLocation, useSearchParams } from "react-router-dom";
import { apiClient } from "../api/client";

function ProductCatalogPage() {
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [searchInput, setSearchInput] = useState(searchParams.get("search") ?? "");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const selectedCategoryId = searchParams.get("categoryId") ?? "";
  const selectedSort = searchParams.get("sort") ?? "";
  const selectedSearch = searchParams.get("search") ?? "";

  useEffect(() => {
    setSearchInput(selectedSearch);
  }, [selectedSearch]);

  useEffect(() => {
    async function loadCatalog() {
      setLoading(true);
      try {
        const [catalogProducts, catalogCategories] = await Promise.all([
          apiClient.getCatalogProducts({
            search: selectedSearch,
            categoryId: selectedCategoryId,
            sort: selectedSort
          }),
          apiClient.getCategories()
        ]);
        setProducts(catalogProducts ?? []);
        setCategories(catalogCategories ?? []);
        setError("");
      } catch (err) {
        setError(err.message || "Unable to load catalog.");
      } finally {
        setLoading(false);
      }
    }

    loadCatalog();
  }, [selectedCategoryId, selectedSearch, selectedSort]);

  function updateFilters(changes) {
    const next = new URLSearchParams(searchParams);

    Object.entries(changes).forEach(([key, value]) => {
      if (!value) {
        next.delete(key);
      } else {
        next.set(key, value);
      }
    });

    setSearchParams(next);
  }

  function handleSubmit(event) {
    event.preventDefault();
    updateFilters({ search: searchInput.trim() });
  }

  return (
    <section className="buyer-page">
      <div className="buyer-page-hero buyer-surface-card">
        <div>
          <p className="eyebrow">Catalog</p>
          <h2>Browse products from the real marketplace backend</h2>
          <p className="muted">
            Search by keyword, filter by category, and inspect any product in detail.
          </p>
        </div>
        <div className="buyer-hero-metrics">
          <div>
            <span>Visible Products</span>
            <strong>{loading ? "..." : products.length}</strong>
          </div>
          <div>
            <span>Categories</span>
            <strong>{categories.length}</strong>
          </div>
        </div>
      </div>

      <div className="table-card">
        <form className="catalog-filter-bar" onSubmit={handleSubmit}>
          <input
            type="text"
            value={searchInput}
            onChange={(event) => setSearchInput(event.target.value)}
            placeholder="Search products..."
          />
          <select value={selectedCategoryId} onChange={(event) => updateFilters({ categoryId: event.target.value })}>
            <option value="">All categories</option>
            {categories.map((category) => (
              <option key={category.categoryId} value={category.categoryId}>
                {category.name}
              </option>
            ))}
          </select>
          <select value={selectedSort} onChange={(event) => updateFilters({ sort: event.target.value })}>
            <option value="">Default sort</option>
            <option value="price_asc">Price: Low to High</option>
            <option value="price_desc">Price: High to Low</option>
            <option value="name_asc">Name: A to Z</option>
            <option value="name_desc">Name: Z to A</option>
            <option value="newest">Newest</option>
            <option value="oldest">Oldest</option>
          </select>
          <button type="submit" className="button button-primary">Apply</button>
        </form>

        {error ? <p className="form-feedback form-feedback-error">{error}</p> : null}

        {loading ? (
          <div className="product-grid">
            {Array.from({ length: 6 }).map((_, index) => (
              <article key={index} className="product-card product-card-skeleton">
                <span className="product-category">Loading</span>
                <h4>Loading product...</h4>
                <p>Fetching catalog entries.</p>
              </article>
            ))}
          </div>
        ) : products.length === 0 ? (
          <p className="empty-cell">No products matched the selected filters.</p>
        ) : (
          <div className="product-grid">
            {products.map((product) => (
              <article key={product.productId} className="product-card">
                <span className="product-category">{product.categoryName}</span>
                <h4>{product.name}</h4>
                <p>Product #{product.productId}</p>
                <div className="product-footer">
                  <div className="price-stack">
                    <strong>{formatCurrency(product.price)}</strong>
                  </div>
                  <Link
                    to={`/products/${product.productId}${location.search}`}
                    className="text-link"
                  >
                    Details
                  </Link>
                </div>
              </article>
            ))}
          </div>
        )}
      </div>
    </section>
  );
}

function formatCurrency(value) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD"
  }).format(Number(value ?? 0));
}

export default ProductCatalogPage;

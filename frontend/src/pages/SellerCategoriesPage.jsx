import { useEffect, useState } from "react";
import { apiClient } from "../api/client";
import { useAuth } from "../auth/AuthContext";

function SellerCategoriesPage() {
  const { accessToken } = useAuth();
  const [categories, setCategories] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadCategories() {
      try {
        const data = await apiClient.getSellerUsedCategories(accessToken);
        setCategories(data ?? []);
      } catch (err) {
        setError(err.message || "Unable to load seller categories.");
      }
    }

    loadCategories();
  }, [accessToken]);

  return (
    <section className="seller-page">
      <div className="section-heading">
        <div>
          <h2>Seller Categories</h2>
          <p className="muted">Categories already used by your products.</p>
        </div>
      </div>

      {error ? <p className="form-feedback form-feedback-error">{error}</p> : null}

      <div className="table-card">
        {categories.length === 0 ? (
          <p className="empty-cell">No categories linked to your products yet.</p>
        ) : (
          <div className="seller-category-grid">
            {categories.map((category) => (
              <article key={category.id} className="seller-category-card">
                <span className="product-category">{category.slug}</span>
                <h3>{category.name}</h3>
              </article>
            ))}
          </div>
        )}
      </div>
    </section>
  );
}

export default SellerCategoriesPage;

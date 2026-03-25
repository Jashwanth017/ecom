import { useEffect, useState } from "react";
import { apiClient } from "../api/client";
import { useAuth } from "../auth/AuthContext";

function AdminCategoriesPage() {
  const { accessToken } = useAuth();
  const [categories, setCategories] = useState([]);
  const [formState, setFormState] = useState({ name: "", slug: "" });
  const [feedback, setFeedback] = useState({ error: "", success: "" });

  useEffect(() => {
    loadCategories();
  }, [accessToken]);

  async function loadCategories() {
    try {
      const data = await apiClient.getAdminCategories(accessToken);
      setCategories(data ?? []);
      setFeedback((previous) => ({ ...previous, error: "" }));
    } catch (err) {
      setFeedback({ error: err.message || "Unable to load categories.", success: "" });
    }
  }

  function handleChange(field) {
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
      const created = await apiClient.createAdminCategory(accessToken, formState);
      setCategories((previous) => [...previous, created].sort((a, b) => a.name.localeCompare(b.name)));
      setFormState({ name: "", slug: "" });
      setFeedback({ error: "", success: "Category created successfully." });
    } catch (err) {
      setFeedback({ error: err.message || "Unable to create category.", success: "" });
    }
  }

  return (
    <section className="seller-page seller-two-column">
      <form className="panel-card seller-form" onSubmit={handleSubmit}>
        <h2>Create Category</h2>
        <p className="muted">Add a new category for sellers to use in products.</p>

        <label>
          Category Name
          <input type="text" value={formState.name} onChange={handleChange("name")} placeholder="Enter category name" />
        </label>

        <label>
          Slug
          <input type="text" value={formState.slug} onChange={handleChange("slug")} placeholder="Optional slug" />
        </label>

        {feedback.error ? <p className="form-feedback form-feedback-error">{feedback.error}</p> : null}
        {feedback.success ? <p className="form-feedback form-feedback-success">{feedback.success}</p> : null}

        <button type="submit" className="button button-primary">Create Category</button>
      </form>

      <div className="table-card">
        <div className="section-heading">
          <div>
            <h2>Category List</h2>
            <p className="muted">All categories available in the marketplace.</p>
          </div>
        </div>

        <div className="seller-category-grid">
          {categories.length === 0 ? (
            <p className="empty-cell">No categories created yet.</p>
          ) : (
            categories.map((category) => (
              <article key={category.id} className="seller-category-card">
                <span className="product-category">{category.slug}</span>
                <h3>{category.name}</h3>
              </article>
            ))
          )}
        </div>
      </div>
    </section>
  );
}

export default AdminCategoriesPage;

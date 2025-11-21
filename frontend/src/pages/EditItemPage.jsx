import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import Navigation from '../components/Navigation';
import ImageUpload from '../components/ImageUpload';
import AIGenerationButton from '../components/AIGenerationButton';
import itemService from '../services/itemService';
import toast from 'react-hot-toast';

const CATEGORIES = [
  'Electronics',
  'Books',
  'Accessories',
  'Sports Equipment',
  'Musical Instruments',
  'Tools',
  'Other',
];

const STATUSES = [
  { value: 'AVAILABLE', label: 'Available' },
  { value: 'BORROWED', label: 'Borrowed' },
  { value: 'UNAVAILABLE', label: 'Unavailable' },
];

const EditItemPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    category: '',
    status: 'AVAILABLE',
    imageUrl: '',
  });
  const [originalData, setOriginalData] = useState(null);
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [showRegenerateTitle, setShowRegenerateTitle] = useState(false);
  const [showRegenerateDescription, setShowRegenerateDescription] = useState(false);
  const [aiAvailable, setAiAvailable] = useState(true);
  const [checkingAI, setCheckingAI] = useState(true);
  const [highlightTitle, setHighlightTitle] = useState(false);
  const [highlightDescription, setHighlightDescription] = useState(false);

  // Check AI availability on component mount
  useEffect(() => {
    const checkAI = async () => {
      setCheckingAI(true);
      const result = await itemService.checkAIAvailability();
      if (result.success && result.data?.data) {
        setAiAvailable(result.data.data.available);
      } else {
        setAiAvailable(false);
      }
      setCheckingAI(false);
    };

    checkAI();
  }, []);

  useEffect(() => {
    const fetchItem = async () => {
      setLoading(true);

      const result = await itemService.getItemById(id);

      if (result.success) {
        const item = result.data;
        
        // Check if user is the owner
        if (user && item.owner?.id !== user.id) {
          toast.error('You are not authorized to edit this item');
          navigate(`/items/${id}`);
          return;
        }

        const itemData = {
          title: item.title || '',
          description: item.description || '',
          category: item.category || '',
          status: item.status || 'AVAILABLE',
          imageUrl: item.imageUrl || '',
        };
        
        setFormData(itemData);
        setOriginalData(itemData);
      } else {
        toast.error(result.error || 'Failed to load item');
        navigate('/my-items');
      }

      setLoading(false);
    };

    fetchItem();
  }, [id, user, navigate]);

  const validateForm = () => {
    const newErrors = {};

    // Title validation
    if (!formData.title.trim()) {
      newErrors.title = 'Title is required';
    } else if (formData.title.trim().length < 3) {
      newErrors.title = 'Title must be at least 3 characters';
    } else if (formData.title.length > 200) {
      newErrors.title = 'Title must not exceed 200 characters';
    }

    // Category validation
    if (!formData.category) {
      newErrors.category = 'Category is required';
    }

    // Status validation
    if (!formData.status) {
      newErrors.status = 'Status is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Clear error for this field
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: undefined,
      }));
    }
  };

  const handleImageUploaded = (imageUrl) => {
    setFormData((prev) => ({
      ...prev,
      imageUrl,
    }));
  };

  const handleTitleGenerated = (generatedTitle) => {
    setFormData((prev) => ({
      ...prev,
      title: generatedTitle,
    }));
    setShowRegenerateTitle(true);
    
    // Highlight the field briefly
    setHighlightTitle(true);
    setTimeout(() => setHighlightTitle(false), 2000);
  };

  const handleDescriptionGenerated = (generatedDescription) => {
    setFormData((prev) => ({
      ...prev,
      description: generatedDescription,
    }));
    setShowRegenerateDescription(true);
    
    // Highlight the field briefly
    setHighlightDescription(true);
    setTimeout(() => setHighlightDescription(false), 2000);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      toast.error('Please fix the errors in the form');
      return;
    }

    setSubmitting(true);

    // Only send changed fields
    const updates = {};
    Object.keys(formData).forEach((key) => {
      if (formData[key] !== originalData[key]) {
        updates[key] = formData[key];
      }
    });

    if (Object.keys(updates).length === 0) {
      toast.info('No changes to save');
      setSubmitting(false);
      return;
    }

    const result = await itemService.updateItem(id, updates);

    if (result.success) {
      toast.success('Item updated successfully!');
      navigate(`/items/${id}`);
    } else {
      toast.error(result.error || 'Failed to update item');
      
      // Handle validation errors from API
      if (result.validationErrors) {
        setErrors(result.validationErrors);
      }
      
      setSubmitting(false);
    }
  };

  const handleCancel = () => {
    navigate(`/items/${id}`);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navigation />
        <div className="flex justify-center items-center py-20">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navigation />
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-6">
          <h1 className="text-3xl font-bold text-gray-900">Edit Item</h1>
          <p className="mt-2 text-gray-600">
            Update your item information
          </p>
        </div>

        <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow-md p-6 space-y-6">
          {/* AI Unavailable Banner */}
          {!aiAvailable && !checkingAI && (
            <div className="p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
              <div className="flex items-start gap-3">
                <svg
                  className="w-6 h-6 text-yellow-600 flex-shrink-0 mt-0.5"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path
                    fillRule="evenodd"
                    d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
                    clipRule="evenodd"
                  />
                </svg>
                <div className="flex-1">
                  <h3 className="text-sm font-semibold text-yellow-800 mb-1">
                    AI Generation Unavailable
                  </h3>
                  <p className="text-sm text-yellow-700">
                    AI-powered content generation is currently disabled. You can still update your item by manually editing the title and description. 
                    Check the helpful tips below each field for guidance.
                  </p>
                </div>
              </div>
            </div>
          )}
          
          {/* Title Input */}
          <div>
            <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-2">
              Title <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              id="title"
              name="title"
              value={formData.title}
              onChange={handleInputChange}
              disabled={submitting}
              className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-300 ${
                errors.title ? 'border-red-500' : 'border-gray-300'
              } ${highlightTitle ? 'bg-green-50 border-green-400 ring-2 ring-green-200' : ''}`}
              placeholder="e.g., Canon EOS Camera"
              maxLength={200}
            />
            {errors.title && (
              <p className="mt-1 text-sm text-red-600">{errors.title}</p>
            )}
            <p className="mt-1 text-sm text-gray-500">
              {formData.title.length}/200 characters
            </p>
            
            {/* AI Generation Button for Title */}
            {aiAvailable && !checkingAI && (
              <div className="mt-3">
                <AIGenerationButton
                  type="title"
                  itemData={{
                    itemName: formData.title,
                    category: formData.category,
                    additionalInfo: formData.description,
                  }}
                  onGenerated={handleTitleGenerated}
                  disabled={submitting || !formData.category}
                  showRegenerate={showRegenerateTitle}
                />
                {!formData.category && (
                  <p className="mt-2 text-xs text-gray-500">
                    Please select a category first to enable AI generation
                  </p>
                )}
              </div>
            )}
            
            {!aiAvailable && !checkingAI && (
              <div className="mt-3 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                <p className="text-sm text-blue-800">
                  💡 <strong>Tip:</strong> Create a clear, descriptive title that includes the item type and key features (e.g., "Canon EOS 80D DSLR Camera with 18-55mm Lens").
                </p>
              </div>
            )}
          </div>

          {/* Description Textarea */}
          <div>
            <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-2">
              Description
            </label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleInputChange}
              disabled={submitting}
              rows={5}
              className={`w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-300 ${
                highlightDescription ? 'bg-green-50 border-green-400 ring-2 ring-green-200' : ''
              }`}
              placeholder="Describe your item, its condition, and any important details..."
            />
            {errors.description && (
              <p className="mt-1 text-sm text-red-600">{errors.description}</p>
            )}
            
            {/* AI Generation Button for Description */}
            {aiAvailable && !checkingAI && (
              <div className="mt-3">
                <AIGenerationButton
                  type="description"
                  itemData={{
                    itemName: formData.title,
                    category: formData.category,
                    additionalInfo: formData.description,
                  }}
                  onGenerated={handleDescriptionGenerated}
                  disabled={submitting || !formData.category}
                  showRegenerate={showRegenerateDescription}
                />
                {!formData.category && (
                  <p className="mt-2 text-xs text-gray-500">
                    Please select a category first to enable AI generation
                  </p>
                )}
              </div>
            )}
            
            {!aiAvailable && !checkingAI && (
              <div className="mt-3 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                <p className="text-sm text-blue-800">
                  💡 <strong>Tip:</strong> Describe the item's condition, key features, and what makes it useful. Mention any accessories included and ideal use cases.
                </p>
              </div>
            )}
          </div>

          {/* Category Dropdown */}
          <div>
            <label htmlFor="category" className="block text-sm font-medium text-gray-700 mb-2">
              Category <span className="text-red-500">*</span>
            </label>
            <select
              id="category"
              name="category"
              value={formData.category}
              onChange={handleInputChange}
              disabled={submitting}
              className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                errors.category ? 'border-red-500' : 'border-gray-300'
              }`}
            >
              <option value="">Select a category</option>
              {CATEGORIES.map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
            {errors.category && (
              <p className="mt-1 text-sm text-red-600">{errors.category}</p>
            )}
          </div>

          {/* Status Dropdown */}
          <div>
            <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-2">
              Status <span className="text-red-500">*</span>
            </label>
            <select
              id="status"
              name="status"
              value={formData.status}
              onChange={handleInputChange}
              disabled={submitting}
              className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                errors.status ? 'border-red-500' : 'border-gray-300'
              }`}
            >
              {STATUSES.map((status) => (
                <option key={status.value} value={status.value}>
                  {status.label}
                </option>
              ))}
            </select>
            {errors.status && (
              <p className="mt-1 text-sm text-red-600">{errors.status}</p>
            )}
          </div>

          {/* Image Upload */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Image
            </label>
            <ImageUpload
              onImageUploaded={handleImageUploaded}
              initialImageUrl={formData.imageUrl}
              disabled={submitting}
            />
            {errors.imageUrl && (
              <p className="mt-1 text-sm text-red-600">{errors.imageUrl}</p>
            )}
          </div>

          {/* Action Buttons */}
          <div className="flex space-x-4 pt-4">
            <button
              type="submit"
              disabled={submitting}
              className="flex-1 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {submitting ? 'Saving...' : 'Save Changes'}
            </button>
            <button
              type="button"
              onClick={handleCancel}
              disabled={submitting}
              className="flex-1 px-6 py-3 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditItemPage;

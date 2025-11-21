import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navigation from '../components/Navigation';
import ItemCard from '../components/ItemCard';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import itemService from '../services/itemService';
import toast from 'react-hot-toast';

/**
 * HomePage component - Landing page with featured items and quick search
 * Requirements: 3.1 - Show featured/recent items, Browse All Items button, quick search bar
 */
const HomePage = () => {
  const navigate = useNavigate();
  const [recentItems, setRecentItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    fetchRecentItems();
  }, []);

  const fetchRecentItems = async () => {
    setLoading(true);
    const result = await itemService.getItems({
      page: 0,
      size: 6,
      status: 'AVAILABLE',
    });

    if (result.success) {
      setRecentItems(result.data.content || []);
    } else {
      toast.error(result.error || 'Failed to load items');
    }
    setLoading(false);
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/items?search=${encodeURIComponent(searchQuery.trim())}`);
    } else {
      navigate('/items');
    }
  };

  const handleBrowseAll = () => {
    navigate('/items');
  };

  return (
    <div className="min-h-screen bg-background">
      <Navigation />
      
      {/* Hero Section */}
      <div className="bg-gradient-to-r from-blue-500 to-purple-600 text-white py-16">
        <div className="container mx-auto px-4">
          <div className="max-w-3xl mx-auto text-center">
            <h1 className="text-5xl font-bold mb-4">
              🎒 Welcome to RentKar
            </h1>
            <p className="text-xl mb-8 opacity-90">
              Share. Borrow. Save Money. Connect with your campus community.
            </p>
            
            {/* Quick Search Bar */}
            <form onSubmit={handleSearch} className="max-w-2xl mx-auto">
              <div className="flex gap-2">
                <Input
                  type="text"
                  placeholder="Search for items..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="flex-1 bg-white text-gray-900 placeholder:text-gray-500"
                />
                <Button type="submit" size="lg" variant="secondary">
                  Search
                </Button>
              </div>
            </form>
          </div>
        </div>
      </div>

      {/* Featured/Recent Items Section */}
      <div className="container mx-auto px-4 py-12">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h2 className="text-3xl font-bold mb-2">Recently Added Items</h2>
            <p className="text-muted-foreground">
              Check out the latest items available for borrowing
            </p>
          </div>
          <Button onClick={handleBrowseAll} size="lg">
            Browse All Items
          </Button>
        </div>

        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[...Array(6)].map((_, index) => (
              <div
                key={index}
                className="bg-white rounded-lg shadow-md overflow-hidden animate-pulse"
              >
                <div className="aspect-square bg-gray-200" />
                <div className="p-4">
                  <div className="h-6 bg-gray-200 rounded mb-2" />
                  <div className="h-4 bg-gray-200 rounded w-2/3 mb-2" />
                  <div className="h-4 bg-gray-200 rounded w-1/2" />
                </div>
              </div>
            ))}
          </div>
        ) : recentItems.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {recentItems.map((item) => (
              <ItemCard key={item.id} item={item} />
            ))}
          </div>
        ) : (
          <div className="text-center py-12">
            <div className="text-6xl mb-4">📦</div>
            <h3 className="text-xl font-semibold mb-2">No items yet</h3>
            <p className="text-muted-foreground mb-6">
              Be the first to list an item on RentKar!
            </p>
            <Button onClick={() => navigate('/items/new')} size="lg">
              Add Your First Item
            </Button>
          </div>
        )}

        {recentItems.length > 0 && (
          <div className="text-center mt-8">
            <Button onClick={handleBrowseAll} variant="outline" size="lg">
              View All Items →
            </Button>
          </div>
        )}
      </div>

      {/* Features Section */}
      <div className="bg-muted py-12">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl font-bold text-center mb-12">
            How RentKar Works
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl mx-auto">
            <div className="text-center">
              <div className="text-5xl mb-4">📝</div>
              <h3 className="text-xl font-semibold mb-2">List Your Items</h3>
              <p className="text-muted-foreground">
                Share items you're not using with your campus community
              </p>
            </div>
            <div className="text-center">
              <div className="text-5xl mb-4">🔍</div>
              <h3 className="text-xl font-semibold mb-2">Find What You Need</h3>
              <p className="text-muted-foreground">
                Browse and search for items available near you
              </p>
            </div>
            <div className="text-center">
              <div className="text-5xl mb-4">🤝</div>
              <h3 className="text-xl font-semibold mb-2">Borrow & Share</h3>
              <p className="text-muted-foreground">
                Connect with peers and save money together
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default HomePage;

import { useNavigate } from 'react-router-dom';

const ItemCard = ({ item }) => {
  const navigate = useNavigate();

  const statusColors = {
    AVAILABLE: 'bg-green-100 text-green-800',
    BORROWED: 'bg-yellow-100 text-yellow-800',
    UNAVAILABLE: 'bg-gray-100 text-gray-800',
  };

  const statusText = {
    AVAILABLE: 'Available',
    BORROWED: 'Borrowed',
    UNAVAILABLE: 'Unavailable',
  };

  const handleClick = () => {
    navigate(`/items/${item.id}`);
  };

  return (
    <div
      onClick={handleClick}
      className="bg-white rounded-lg shadow-md overflow-hidden cursor-pointer hover:shadow-lg transition-shadow duration-200"
    >
      <div className="aspect-square bg-gray-200 relative">
        {item.imageUrl ? (
          <img
            src={item.imageUrl}
            alt={item.title}
            className="w-full h-full object-cover"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-gray-400">
            <svg
              className="w-16 h-16"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
          </div>
        )}
        <span
          className={`absolute top-2 right-2 px-2 py-1 rounded-full text-xs font-medium ${
            statusColors[item.status] || statusColors.UNAVAILABLE
          }`}
        >
          {statusText[item.status] || 'Unknown'}
        </span>
      </div>
      <div className="p-4">
        <h3 className="font-semibold text-lg mb-1 truncate">{item.title}</h3>
        <p className="text-sm text-gray-600 mb-2">{item.category}</p>
        <div className="flex items-center text-sm text-gray-500">
          <svg
            className="w-4 h-4 mr-1"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
            />
          </svg>
          <span className="truncate">{item.owner?.username || 'Unknown'}</span>
        </div>
      </div>
    </div>
  );
};

export default ItemCard;

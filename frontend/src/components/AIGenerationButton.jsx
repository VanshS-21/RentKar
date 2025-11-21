import { useState, useEffect } from 'react';
import { Sparkles, Loader2, RefreshCw, Zap, CheckCircle } from 'lucide-react';
import useAIGeneration from '../hooks/useAIGeneration';

/**
 * AIGenerationButton component for generating AI content
 * @param {Object} props
 * @param {string} props.type - Type of content ('title' or 'description')
 * @param {Object} props.itemData - Item data for generation
 * @param {Function} props.onGenerated - Callback when content is generated
 * @param {boolean} props.disabled - Whether button is disabled
 * @param {boolean} props.showRegenerate - Whether to show regenerate button
 * @param {string} props.className - Additional CSS classes
 */
const AIGenerationButton = ({
  type,
  itemData,
  onGenerated,
  disabled = false,
  showRegenerate = false,
  className = '',
}) => {
  const {
    generate,
    regenerate,
    loading,
    error,
    success,
    remainingRequests,
    retryAfter,
    isRateLimited,
    clearError,
  } = useAIGeneration(type, itemData, onGenerated);

  const [countdown, setCountdown] = useState(0);
  const [loadingDots, setLoadingDots] = useState('');

  // Handle countdown timer for rate limit
  useEffect(() => {
    if (retryAfter && retryAfter > 0) {
      setCountdown(retryAfter);
      
      const timer = setInterval(() => {
        setCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);

      return () => clearInterval(timer);
    }
  }, [retryAfter]);

  // Animate loading dots
  useEffect(() => {
    if (loading) {
      const dotsTimer = setInterval(() => {
        setLoadingDots((prev) => {
          if (prev === '...') return '';
          return prev + '.';
        });
      }, 500);

      return () => clearInterval(dotsTimer);
    } else {
      setLoadingDots('');
    }
  }, [loading]);

  const handleClick = async () => {
    clearError();
    if (showRegenerate) {
      await regenerate();
    } else {
      await generate();
    }
  };

  const isDisabled = disabled || loading || isRateLimited;
  const buttonText = showRegenerate ? 'Regenerate' : `Generate ${type === 'title' ? 'Title' : 'Description'} with AI`;
  const Icon = showRegenerate ? RefreshCw : Sparkles;

  // Format countdown time
  const formatCountdown = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return mins > 0 ? `${mins}m ${secs}s` : `${secs}s`;
  };

  return (
    <div className={`space-y-2 ${className}`} role="region" aria-label="AI content generation">
      <div className="flex items-center gap-2">
        <button
          type="button"
          onClick={handleClick}
          disabled={isDisabled}
          className={`
            inline-flex items-center gap-2 px-4 py-2 rounded-lg font-medium
            transition-all duration-200 ease-in-out
            focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500
            ${
              isDisabled
                ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                : 'bg-gradient-to-r from-purple-600 to-blue-600 text-white hover:from-purple-700 hover:to-blue-700 hover:shadow-lg transform hover:-translate-y-0.5 active:scale-95'
            }
            disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none
          `}
          title={
            isRateLimited
              ? `Rate limit reached. Try again in ${formatCountdown(countdown)}`
              : `${buttonText} - Uses AI to generate content`
          }
          aria-label={
            loading
              ? `Generating ${type} with AI, please wait`
              : isRateLimited
              ? `AI generation rate limit reached. Try again in ${formatCountdown(countdown)}`
              : `${buttonText} using artificial intelligence`
          }
          aria-busy={loading}
          aria-disabled={isDisabled}
        >
          {loading ? (
            <>
              <Loader2 className="w-4 h-4 animate-spin" aria-hidden="true" />
              <span className="inline-flex items-center">
                Generating
                <span className="inline-block w-6 text-left">{loadingDots}</span>
              </span>
            </>
          ) : (
            <>
              <Icon className="w-4 h-4" aria-hidden="true" />
              <span>{buttonText}</span>
            </>
          )}
        </button>

        {/* Remaining requests indicator with tooltip */}
        {remainingRequests !== null && !isRateLimited && (
          <div className="relative group">
            <span
              className={`text-sm font-medium transition-colors duration-200 cursor-help ${
                remainingRequests <= 3
                  ? 'text-red-600'
                  : remainingRequests <= 5
                  ? 'text-yellow-600'
                  : 'text-green-600'
              }`}
              aria-label={`${remainingRequests} AI generation${remainingRequests !== 1 ? 's' : ''} remaining this hour`}
              role="status"
            >
              {remainingRequests} left
            </span>
            {/* Tooltip */}
            <div className="absolute left-0 top-full mt-2 w-64 p-3 bg-gray-900 text-white text-xs rounded-lg shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-10">
              <p className="font-semibold mb-1">Rate Limit Information</p>
              <p>You have {remainingRequests} AI generation{remainingRequests !== 1 ? 's' : ''} remaining this hour.</p>
              <p className="mt-1">Limit: 10 generations per hour</p>
              <div className="absolute -top-1 left-4 w-2 h-2 bg-gray-900 transform rotate-45"></div>
            </div>
          </div>
        )}
      </div>

      {/* Loading progress indicator */}
      {loading && (
        <div className="flex items-center gap-2 p-3 bg-purple-50 border border-purple-200 rounded-lg animate-pulse">
          <Zap className="w-5 h-5 text-purple-600 animate-bounce" aria-hidden="true" />
          <div className="flex-1">
            <p className="text-sm text-purple-800 font-medium">
              AI is generating your {type}...
            </p>
            <div className="mt-2 w-full bg-purple-200 rounded-full h-1.5 overflow-hidden">
              <div className="h-full bg-gradient-to-r from-purple-600 to-blue-600 rounded-full animate-progress"></div>
            </div>
          </div>
        </div>
      )}

      {/* Success message */}
      {success && !loading && (
        <div className="flex items-center gap-2 p-3 bg-green-50 border border-green-200 rounded-lg animate-fade-in">
          <CheckCircle className="w-5 h-5 text-green-600" aria-hidden="true" />
          <div className="flex-1">
            <p className="text-sm text-green-800 font-medium">
              ✨ {type === 'title' ? 'Title' : 'Description'} generated successfully!
            </p>
          </div>
        </div>
      )}

      {/* Error message */}
      {error && (
        <div className="flex items-start gap-2 p-3 bg-red-50 border border-red-200 rounded-lg">
          <svg
            className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5"
            fill="currentColor"
            viewBox="0 0 20 20"
          >
            <path
              fillRule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
              clipRule="evenodd"
            />
          </svg>
          <div className="flex-1">
            <p className="text-sm text-red-800">{error}</p>
            {isRateLimited && countdown > 0 && (
              <p className="text-xs text-red-600 mt-1">
                Try again in {formatCountdown(countdown)}
              </p>
            )}
            {!isRateLimited && (
              <button
                type="button"
                onClick={handleClick}
                className="mt-2 text-xs text-red-700 hover:text-red-900 underline font-medium"
              >
                Try again
              </button>
            )}
          </div>
        </div>
      )}

      {/* Rate limit warning */}
      {isRateLimited && countdown > 0 && !error && (
        <div className="flex items-start gap-2 p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
          <svg
            className="w-5 h-5 text-yellow-600 flex-shrink-0 mt-0.5"
            fill="currentColor"
            viewBox="0 0 20 20"
          >
            <path
              fillRule="evenodd"
              d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
              clipRule="evenodd"
            />
          </svg>
          <div className="flex-1">
            <p className="text-sm text-yellow-800">
              Rate limit reached. You can generate more content in {formatCountdown(countdown)}.
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default AIGenerationButton;

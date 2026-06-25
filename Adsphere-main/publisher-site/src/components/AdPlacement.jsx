import React, { useEffect, useRef, useState } from 'react';

const AdPlacement = ({ placementId, prefetchedAd, type, fallbackImageUrl }) => {
  const adRef = useRef(null);
  const [impressionRecorded, setImpressionRecorded] = useState(false);
  const [adData, setAdData] = useState(prefetchedAd || null);

  useEffect(() => {
    if (prefetchedAd) {
      setAdData(prefetchedAd);
      setImpressionRecorded(true);
      return;
    }

    if (!placementId) return;

    // Fetch the actual ad from the backend
    fetch(`/api/track/serve/${placementId}`)
      .then(res => {
        if (res.ok) return res.json();
        throw new Error("Ad not found");
      })
      .then(data => {
        setAdData(data);
        // Note: The backend /serve endpoint already records the impression
        setImpressionRecorded(true); 
      })
      .catch(err => console.error("Failed to fetch ad:", err));
  }, [placementId, prefetchedAd]);

  useEffect(() => {
    // If we didn't fetch an ad successfully, we still want to trigger 
    // an impression if we show the fallback ad when scrolled into view.
    if (!placementId || impressionRecorded || !fallbackImageUrl) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && !impressionRecorded) {
          fetch(`/api/track/impression/${placementId}`, { method: 'POST' })
            .then(res => {
              if (res.ok) setImpressionRecorded(true);
            })
            .catch(err => console.error("Failed to record impression:", err));
        }
      },
      { threshold: 0.5 }
    );

    if (adRef.current) {
      observer.observe(adRef.current);
    }

    return () => {
      if (adRef.current) observer.unobserve(adRef.current);
    };
  }, [placementId, impressionRecorded, fallbackImageUrl]);

  const handleClick = () => {
    if (!placementId) return;
    
    const clickUrl = adData && adData.campaignId
      ? `/api/track/click/${placementId}?campaignId=${adData.campaignId}`
      : `/api/track/click/${placementId}`;
      
    // Record click in the backend
    fetch(clickUrl, { method: 'POST' })
      .catch(err => console.error("Failed to record click:", err));
      
    // Open the advertiser's destination link in a new tab if it exists
    if (adData && adData.destinationUrl) {
      window.open(adData.destinationUrl, '_blank');
    }
  };

  const adClass = type === 'banner' ? 'ad-banner' : 'ad-sidebar';
  
  // Use fetched ad image if available, otherwise use fallback
  const displayImageUrl = adData ? adData.imageUrl : fallbackImageUrl;
  const displayTitle = adData ? adData.title : "Advertisement";

  return (
    <div 
      ref={adRef} 
      className={`ad-container ${adClass}`} 
      onClick={handleClick}
      title={displayTitle}
    >
      {displayImageUrl ? (
        <img src={displayImageUrl} alt={displayTitle} />
      ) : (
        <div style={{ color: 'var(--text-secondary)' }}>
          Ads by AdSphere
        </div>
      )}
    </div>
  );
};

export default AdPlacement;

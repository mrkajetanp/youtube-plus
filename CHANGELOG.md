# Changelog

---

#### 9.02.2019

* [General] Fragment-specific option menus

#### 8.02.2019

* [General] Reduced SettingsActivity to a single Fragment
* [General] Fixed clearing searchQuery after popping back the VideoListFragment

#### 7.02.2019

* [General] ActionBar navigation integration
* [VideoListFragment] Opening channel's videos onClick in search list

#### 6.02.2019

* [General] PlaylistContentFragment on channel click
* [MainActivity] Fixed dark mode bottom bar bug

#### 5.02.2019

* [General] LibraryFragment with a bottom bar option
* [General] Refactoring, improvements and string resources

#### 4.02.2019

* [Favourites] Switched favourite videos with playlists 
* [Database] Added database table for playlists

#### 3.02.2019

* [PlayerActivity] Fixed playlist video index incorrectly updating
* [VideoListFragment] Opening a playlist on click in search results

#### 2.02.2019

* [VideoListFragment] Playlists and channels in search results 
* [ContentListAdapter] Different binding for different item types

#### 1.02.2019

* [ContentListAdapter] Switched the Adapter to use FeedItem 
* [YouTubeData] Getting different type data into a list of FeedItems

#### 31.01.2019

* [General] Renamed the adapter; planning
* [YouTubeData] Distinguishing between search response item types

#### 30.01.2019

* [YouTubeData] Removed unnecessary class field 
* [General] Minor improvements

#### 29.01.2019

* [VideoListAdapter] Playlist bold now playing, TODOs 
* [General] Changed the icon to the final one

#### 28.01.2019

* [General] Fixed a layout bug in Player's playlist 
* [General] Fixed a minor Manifest bug

#### 27.01.2019

* [General] Updating the theme accordingly 
* [MainAcivity] Added a BottomBar separator

#### 26.01.2019

* [General] Mostly functional Dark Theme by default 
* [General] Added SettingsActivity with Dark Mode option

#### 25.01.2019

* [General] Code improvements and cleanup
* [General] Replaced startActivity calls with Navigation Component

#### 24.01.2019

* [PlayerActivity] Fixed bugs on share intent edge cases 
* [PlayerActivity] Fixed minor bugs related to onNewIntent

#### 23.01.2019

* [PlayerActivity] Playlist: Original YouTube-like playlist appearance
* [PlayerActivity] Playlist: Playing the next video automatically

#### 22.01.2019

* [General] Removed the now unnecessary PlaylistContentAdapter 
* [PlayerActivity] Loading more playlist videos on scrolling down

#### 21.01.2019

* [General] Reorganised the general code structure
* [OthersFragment] Removed unnecessary code, added Where do We Go From Here button

#### 20.01.2019

* [General] Fixed SearchView behaviour with Navigation Component
* [General] Migrated to Jetpack Navigation Component

#### 19.01.2019

* [PlayerActivity] Fixed a bug with late ViewModel initialisation 
* [PlayerActivity] Converted FavouritesAdapter to PlaylistContentAdapter

#### 18.01.2019

* [PlayerActivity] Playlist RecyclerView in PlayerActivity
* [PlayerActivity] Playing the first video on receiving a playlist

#### 17.01.2019

* [PlayerActivity] Fixed getting playlist data
* [PlayerActivity] Updating menu item state after adding to favourites

#### 16.01.2019

* [PlayerActivity] Fixed broken Seek dialog 
* [PlayerActivity] Added an option to remove from favourites

#### 15.01.2019

* [PlayerActivity] Refactoring and improvements
* [PlayerActivity] Fixed switching to a new video on a new share Intent

#### 14.01.2019

* [Favourites] Clearing the search query on reseting to mostPopular 
* [Favourites] Fixed filtering with an optional callback

#### 13.01.2019

* [General] Refactoring and improvements 
* [General] Fixed retaining the bottom bar state

#### 12.01.2019

* [General] Filtering videos in FavouritesFragment
* [General] Converted favourites to a Fragment

#### 11.01.2019

* [VideoListFragment] General refactoring
* [General] Converted video search to a Fragment

#### 10.01.2019

* [PlayerActivity] Fixed the notification after video ends
* [PlayerActivity] Fixed the playPause action on the notification

#### 9.01.2019

* [PlayerActivity] Minor fixes 
* [PlayerActivity] Fixed a bug by switching to a dynamic BroadcastReceiver

#### 8.01.2019

* [General] Minor fixes 
* [General] Migrated to AndroidX

#### 7.01.2019

* [YouTubeData] Changed app theme to light Material Design
* [YouTubeData] Reduced unnecessary service.build() calls

#### 6.01.2019

* [YouTubeData] Fixed a bug in video search
* [General] Converted YouTubeData to Kotlin

#### 5.01.2019

* [VideoList] Proper video duration parsing 
* [General] SeekDialog Kotlin improvements
* [General] Converted SeekDialog to Kotlin

#### 4.01.2019

* [General] PlayerActivity Kotlin improvements
* [General] Converted PlayerActivity to Kotlin

#### 3.01.2019

* [General] Converted VideoListAdapter to Kotlin
* [General] Converted FavouriteListAdapter to Kotlin

#### 2.01.2019

* [General] Converted ids to camel case 
* [General] Converted MainActivity to Kotlin

#### 1.01.2019

* [General] Refactored Kotlin code using Kotlin Android Extensions 
* [General] Refactored Kotlin code using Anko library

#### 31.12.2018

* [General] Converted the database to Kotlin and added improvements
* [General] Converted most of the database to Kotlin

#### 30.12.2018

* [FavouritesActivity] Removed Adapter nullability 
* [General] Converted FavouritesActivity to Kotlin

#### 29.12.2018

* [General] Converted FullScreenHelper to Kotlin

#### 22.12.2018

* [Favourites] Added a message when there are no favourites
* [Favourites] Fixed a bug occurring when there are no favourites

#### 21.12.2018

* [YouTubeData] Getting playlist data
* [PlayerActivity] Getting playlist ID if shared

#### 20.12.2018

* [General] Converted various strings to resources 
* [General] Video list item selector and layout tweaks

#### 19.12.2018

* [General] Live video UI and LIVE 'duration' in a search list
* [MainActivity] Adding to favourites onLongClick with a confirmation

#### 18.12.2018

* [Favourites] Removal confirmation dialog 
* [General] Refactoring and cleanup

#### 17.12.2018

* [General] Switched most popular's country to GB 
* [General] Showing most popular videos on app startup

#### 16.12.2018

* [YouTubeData] Started implementing mostPopular on start 
* [YouTubeData] Reduced the amount of API requests

#### 15.12.2018

* [Favourites] Removing favourite on hold and removed bottom bar from PlayerActivity
* [Favourites] Playing on click and progress bar

#### 14.12.2018

* [Favourites] Working RecyclerView with favourite videos 
* [Favourites] Changed 'Starred' to 'Favourites' and added Database delete

#### 13.12.2018

* [Starred] Functional Star button in PlayerActivity 
* [Starred] Operational Room database for starred videos

#### 12.12.2018

* [Starred] Bottom bar in the StarredActivity 
* [Starred] Added a 'Star' button and StarredActivity

#### 11.12.2018

* [General] Minor refactoring 
* [General] Updated to API 28

#### 10.12.2018

* [MainActivity] Added a bottom bar to this activity as well
* [MainActivity] Fixed a minor issue with SearchView focusability 

#### 9.12.2018

* [MainActivity] Proper Search Widget instead of an EditText

#### 8.12.2018

* [VideoListAdapter] Reorganised and categorised methods
* [MainActivity] Reorganised and categorised methods

#### 7.12.2018

* [PlayerActivity] Reorganised and categorised methods
* [PlayerActivity] Bottom bar and seek menu option icons

#### 6.12.2018

* [BottomBar] Improved bottom bar colour and state behaviour 
* [BottomBar] Simple bottom navigation bar in the PlayerActivity

#### 5.12.2018

* [SeekDialog] Setting the time to the current video position 
* [SeekDialog] Dismissing the menu on click

#### 4.12.2018

* [SeekDialog] Setting proper max values in sliders and hiding them accordingly 
* [SeekDialog] Converting time to a float inside the class

#### 3.12.2018

* [PlayerActivity] Working seek functionality 
* [PlayerActivity] Seek dialog layout setup

#### 2.12.2018

* [PlayerActivity] Basic dialog for video seek 
* [PlayerActivity] Video menu with seek to 0 option

#### 1.12.2018

* [YouTubeData] Minor refactors and improvements 
* [YouTubeData] Method structure refactoring

#### 30.11.2018

* [General] Minor changes and improvements 
* [VideoSearch] Added video duration on thumbnails

#### 29.11.2018

* [VideoSearch] YouTube app-like grey background before thumbnail loads 
* [VideoSearch] Further improved thumbnail dimensions

#### 28.11.2018

* [VideoSearch] Improved thumbnails by using the Picasso library 
* [VideoSearch] Improved adding new results to the adapter

#### 27.11.2018

* [VideoSearch] Better jumping to position after loading more videos
* [VideoSearch] Further scrolling with a bottom progress bar

#### 26.11.2018

* [VideoSearch] Merging adapter data and unlimited scrolling
* [VideoSearch] Next search page on reaching bottom

#### 25.11.2018

* [VideoSearch] Search & page button behaviour fixes
* [VideoSearch] Working previous page button

#### 24.11.2018

* [VideoSearch] Working next page button 
* [VideoSearch] Getting the next page token

#### 23.11.2018

* [VideoSearch] Removed the test button
* [VideoSearch] Adjusted thumbnail size to match original YouTube app

#### 22.11.2018

* [VideoSearch] Channel title & thumbnail in search results 
* [VideoSearch] Playing video on result click

#### 21.11.2018

* [VideoSearch] Working RecyclerView with search results 
* [VideoSearch] Started building a RecyclerView adapter

#### 18.11.2018

* [General] Replaced the search button with keyboard search action
* [General] Added a search progress bar 
* [General] Fixed a bug with search account chooser

#### 17.11.2018

* [General] Started implementing search
* [General] Added translatable string markers

#### 16.11.2018

* [General] Experiments with launch modes
* [General] Cleanup & logging

#### 15.11.2018

* [General] Refactored showMediaNotification 
* [General] Fixed a bug with selecting a Google account

#### 14.11.2018

* [PlayerActivity] Extracted getting the videoId to a method 
* [PlayerActivity] Dismissing the notification after closing the app

#### 13.11.2018

* [YouTubeData] Further Refactoring and small adjustments
* [YouTubeData] Refactoring and TODOs

#### 12.11.2018

* [General] Implemented setting media notification data

#### 11.11.2018

* [General] Added a Toast with the video title 
* [General - Dev] Managed to get data from the API

#### 10.11.2018

* [General - Dev] Started implementing YouTubeData API call

#### 8.11.2018

* [General] Gradle versioning system 
* [General] Fixed a minor PlayerActivity Intent bug

#### 7.11.2018

* [General] Refactoring, extracting Strings, comments 
* [General] Moved the Player to a separate Activity

#### 6.11.2018

* [Player] Functional Media Notification with Play/Pause 
* [Player] Created a Media Channel

#### 5.11.2018

* [Player] Started implementing the media notification 
* [Player] Setting state builder to match the player

#### 4.11.2018

* [Player] Media Session to support media buttons 
* [General] Improved launcher icon quality

#### 3.11.2018

* [General] Player setup in a separate method

#### 2.11.2018

* [Player] Proper fullscreen mode, removed the ActionBar

#### 1.11.2018

* [General] Added the app icon 
* [Player] Handling screen rotation

#### 31.10.2018

* [General] Some cleanup

#### 30.10.2018

* [Library] Override background visibility only if playing

#### 29.10.2018

* [Library] Implemented background playback as an option 
* [Player] Accepting & handling YouTube share intents

#### 28.10.2018

* [Player] Implemented background video playback in the library

#### 26.10.2018

* [General] android-youtube-player lifecycle experiments 
* [General] android-youtube-player as a module

#### 25.10.2018

* [General] Video player 
* [General] Project & repository setup


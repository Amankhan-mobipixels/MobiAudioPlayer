# Mobi Audio Player
add maven in your project level gradle
````
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' 
		}
	}
}
````
add dependency in module level gradle
````
dependencies:
{
   implementation 'com.github.Amankhan-mobipixels:MobiVideoPlayer:1.0.5'
}

````
How to use:

        val intent = Intent(this, MobiAudioPlayer::class.java)
        intent.putExtra("playListName","Downloads")
        intent.putExtra("data",getFilePath())
        startActivity(intent)
   
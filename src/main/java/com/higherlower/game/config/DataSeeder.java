package com.higherlower.game.config;
import com.higherlower.game.entity.Item;
import com.higherlower.game.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.util.List;
@Component
@Profile({"dev","default","prod"})
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {
    private final ItemRepository itemRepository;
    @Override
    public void run(String... args) {
        if (itemRepository.countByActiveTrue() > 0) {
            log.info("DataSeeder: items already present - skipping seed.");
            return;
        }
        log.info("DataSeeder: seeding sample items...");
        List<Item> items = List.of(
            Item.builder()
                .title("Attack on Titan")
                .imageUrl("https://m.media-amazon.com/images/M/MV5BZjliODY5MzQtMmViZC00MTZmLWFhMWMtMjMwM2I3OGY1MTRiXkEyXkFqcGc@._V1_FMjpg_UX1000_.jpg")
                .searchVolume(6120000L)
                .category("Anime")
                .build(),
            Item.builder()
                .title("Death Note")
                .imageUrl("https://i.pinimg.com/736x/04/8a/fc/048afcc1d1f6b331a63357c6c7fee96d.jpg")
                .searchVolume(4090000L)
                .category("Anime")
                .build(),
            Item.builder()
                .title("Demon Slayer")
                .imageUrl("https://i.pinimg.com/736x/c3/ae/f3/c3aef308a3bed8c48d64be55aa4ccf4c.jpg")
                .searchVolume(6120000L)
                .category("Anime")
                .build(),
            Item.builder()
                .title("Dragon Ball Z")
                .imageUrl("https://images.unsplash.com/photo-1606894436761-7a742916220a?q=80&w=687")
                .searchVolume(7480000L)
                .category("Anime")
                .build(),
            Item.builder()
                .title("Jujutsu Kaisen")
                .imageUrl("https://i.pinimg.com/736x/76/24/f6/7624f688804a3cafc9602e0432d5556d.jpg")
                .searchVolume(5000000L)
                .category("Anime")
                .build(),
            Item.builder()
                .title("My Hero Academia")
                .imageUrl("https://i.pinimg.com/736x/97/5a/8a/975a8a1d3d8281901d3e3b4f9e13c04d.jpg")
                .searchVolume(4090000L)
                .category("Anime")
                .build(),
            Item.builder()
                .title("Naruto")
                .imageUrl("https://images.unsplash.com/photo-1630710478039-9c680b99f800?q=80&w=1170")
                .searchVolume(9140000L)
                .category("Anime")
                .build(),
            Item.builder()
                .title("One Piece")
                .imageUrl("https://images.unsplash.com/photo-1734517709196-48873cca9599?q=80&w=687")
                .searchVolume(9140000L)
                .category("Anime")
                .build(),
            Item.builder()
                .title("Apple")
                .imageUrl("https://images.unsplash.com/photo-1614312385003-dcea7b8b6ab6?q=80&w=1326")
                .searchVolume(37200000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("Elon Musk")
                .imageUrl("https://i.pinimg.com/1200x/5e/d5/83/5ed58333fc3f990433d888e5add68be3.jpg")
                .searchVolume(20400000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("Nike")
                .imageUrl("https://images.unsplash.com/photo-1600269452121-4f2416e55c28?q=80&w=765")
                .searchVolume(13600000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("Samsung")
                .imageUrl("https://i.pinimg.com/736x/8b/7f/8f/8b7f8ffb83f7a6e29a91110aa626b4fe.jpg")
                .searchVolume(20400000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("Adele")
                .imageUrl("https://media.newyorker.com/photos/6169d1caf9c7cf02c89a9454/master/pass/Battan-AdeleEasyOnMe-2.jpg")
                .searchVolume(9140000L)
                .category("Entertainment")
                .build(),
            Item.builder()
                .title("Bad Bunny")
                .imageUrl("https://i.pinimg.com/736x/33/d4/5f/33d45f51ec380c7b3aaf3f51f5c40806.jpg")
                .searchVolume(13600000L)
                .category("Entertainment")
                .build(),
            Item.builder()
                .title("Blackpink")
                .imageUrl("https://i.pinimg.com/736x/e9/56/ee/e956ee7a7e8ab4304acdca02e63174e7.jpg")
                .searchVolume(20400000L)
                .category("Entertainment")
                .build(),
            Item.builder()
                .title("BTS")
                .imageUrl("https://ew.com/thmb/dUJuFxoLGZih8KCsaRPWJi1Zx1Q=/2000x0/filters:no_upscale():max_bytes(150000):strip_icc():format(webp)/bts-d0068f30c5e6448cb126f6e8d4529566.jpg")
                .searchVolume(27100000L)
                .category("Entertainment")
                .build(),
            Item.builder()
                .title("Coldplay")
                .imageUrl("https://i.pinimg.com/736x/f2/54/31/f25431a925c7288464cebae0cea7ccc0.jpg")
                .searchVolume(9140000L)
                .category("Entertainment")
                .build(),
            Item.builder()
                .title("Drake")
                .imageUrl("https://i.pinimg.com/736x/94/11/bb/9411bbf53f68c6db841b54ace7e86b5d.jpg")
                .searchVolume(18100000L)
                .category("Entertainment")
                .build(),
            Item.builder()
                .title("Ed Sheeran")
                .imageUrl("https://cdn.britannica.com/17/249617-050-4575AB4C/Ed-Sheeran-performs-Rockefeller-Plaza-Today-Show-New-York-2023.jpg")
                .searchVolume(11100000L)
                .category("Entertainment")
                .build(),
            Item.builder()
                .title("Eminem")
                .imageUrl("https://cdn.britannica.com/64/136264-050-EE4D5B0F/Eminem-2009.jpg")
                .searchVolume(11100000L)
                .category("Entertainment")
                .build(),
            Item.builder()
                .title("Justin Bieber")
                .imageUrl("https://i.pinimg.com/1200x/77/78/3d/77783db044bc8fe7e0ec0f1b4072e658.jpg")
                .searchVolume(18100000L)
                .category("Entertainment")
                .build(),
            Item.builder()
                .title("MrBeast")
                .imageUrl("https://i.pinimg.com/736x/e5/68/2b/e5682b6afcdd53648c8b245530784cd0.jpg")
                .searchVolume(22200000L)
                .category("Entertainment")
                .build(),
            Item.builder()
                .title("PewDiePie")
                .imageUrl("https://i.pinimg.com/736x/ce/52/5c/ce525ca8c77b48e42e6733acc17e64ae.jpg")
                .searchVolume(9140000L)
                .category("Entertainment")
                .build(),
            Item.builder()
                .title("Rihanna")
                .imageUrl("https://hips.hearstapps.com/hmg-prod/images/rihanna-attends-the-savage-x-fenty-celebration-of-lavish-news-photo-1758829666.pjpeg?crop=0.516xw:0.344xh;0.283xw,0.0645xh&resize=640:*")
                .searchVolume(18100000L)
                .category("Entertainment")
                .build(),
            Item.builder()
                .title("Taylor Swift")
                .imageUrl("https://i.pinimg.com/736x/fe/2f/5c/fe2f5c3db9f9f9263b21823f29baa794.jpg")
                .searchVolume(20400000L)
                .category("Entertainment")
                .build(),
            Item.builder()
                .title("The Beatles")
                .imageUrl("https://i.pinimg.com/736x/26/64/21/2664218536866d138c8588a90ff469a9.jpg")
                .searchVolume(4090000L)
                .category("Entertainment")
                .build(),
            Item.builder()
                .title("Budweiser")
                .imageUrl("https://images.unsplash.com/photo-1608270586620-248524c67de9?w=800")
                .searchVolume(301000L)
                .category("Food and Drink")
                .build(),
            Item.builder()
                .title("Burger King")
                .imageUrl("https://i.pinimg.com/736x/fe/83/e3/fe83e3cdeccfe513d1f3cf0a58231db6.jpg")
                .searchVolume(5000000L)
                .category("Food and Drink")
                .build(),
            Item.builder()
                .title("Coca-Cola")
                .imageUrl("https://images.unsplash.com/photo-1554866585-cd94860890b7?w=800")
                .searchVolume(9140000L)
                .category("Food and Drink")
                .build(),
            Item.builder()
                .title("Corn Flakes")
                .imageUrl("https://images.unsplash.com/photo-1506368197720-c242fdaa44dc?q=80&w=687")
                .searchVolume(450000L)
                .category("Food and Drink")
                .build(),
            Item.builder()
                .title("Dominos")
                .imageUrl("https://i.pinimg.com/736x/d5/85/35/d585359a11240b3b9e8642f0afc8d9c5.jpg")
                .searchVolume(3350000L)
                .category("Food and Drink")
                .build(),
            Item.builder()
                .title("KFC")
                .imageUrl("https://i.pinimg.com/736x/6b/c9/52/6bc95249b3247a3adf1bcca89d55b87e.jpg")
                .searchVolume(6120000L)
                .category("Food and Drink")
                .build(),
            Item.builder()
                .title("Lays")
                .imageUrl("https://i.pinimg.com/1200x/89/31/aa/8931aab4106609fa13616d52c30b131f.jpg")
                .searchVolume(1500000L)
                .category("Food and Drink")
                .build(),
            Item.builder()
                .title("McDonald s")
                .imageUrl("https://images.unsplash.com/photo-1572802419224-296b0aeee0d9?w=800")
                .searchVolume(13600000L)
                .category("Food and Drink")
                .build(),
            Item.builder()
                .title("McDonald s Big Mac")
                .imageUrl("https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800")
                .searchVolume(2240000L)
                .category("Food and Drink")
                .build(),
            Item.builder()
                .title("Pizza Hut")
                .imageUrl("https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800")
                .searchVolume(4090000L)
                .category("Food and Drink")
                .build(),
            Item.builder()
                .title("Red Bull")
                .imageUrl("https://i.pinimg.com/736x/97/49/eb/9749eb98e9114b3b8d7d19409c39e50c.jpg")
                .searchVolume(3350000L)
                .category("Food and Drink")
                .build(),
            Item.builder()
                .title("Starbucks")
                .imageUrl("https://images.unsplash.com/photo-1577215451400-f207c63e30be?q=80&w=1170")
                .searchVolume(7480000L)
                .category("Food and Drink")
                .build(),
            Item.builder()
                .title("Among Us")
                .imageUrl("https://i.pinimg.com/1200x/1e/06/5c/1e065cb0cfb8e593a221589d1ed7f315.jpg")
                .searchVolume(9140000L)
                .category("Gaming")
                .build(),
            Item.builder()
                .title("Call of Duty")
                .imageUrl("https://i.pinimg.com/736x/47/f6/7d/47f67d4796f17a189a7f353e6a0f6224.jpg")
                .searchVolume(13600000L)
                .category("Gaming")
                .build(),
            Item.builder()
                .title("Candy Crush")
                .imageUrl("https://i.pinimg.com/736x/ee/eb/89/eeeb89777dfb605186e54077693b0f98.jpg")
                .searchVolume(5000000L)
                .category("Gaming")
                .build(),
            Item.builder()
                .title("Clash of Clans")
                .imageUrl("https://i.pinimg.com/736x/02/3d/6b/023d6bdf4acabd7647bda7bae0c6486a.jpg")
                .searchVolume(6120000L)
                .category("Gaming")
                .build(),
            Item.builder()
                .title("FIFA")
                .imageUrl("https://i.pinimg.com/736x/26/72/3b/26723bed983c5fed1b78413c06027e9e.jpg")
                .searchVolume(20400000L)
                .category("Gaming")
                .build(),
            Item.builder()
                .title("Fortnite")
                .imageUrl("https://i.pinimg.com/736x/7e/e8/c4/7ee8c4361736ed806711ae99f7d6762c.jpg")
                .searchVolume(18100000L)
                .category("Gaming")
                .build(),
            Item.builder()
                .title("GTA 5")
                .imageUrl("https://4kwallpapers.com/images/walls/thumbs_3t/10749.jpg")
                .searchVolume(18100000L)
                .category("Gaming")
                .build(),
            Item.builder()
                .title("League of Legends")
                .imageUrl("https://i.pinimg.com/736x/20/54/b6/2054b617ac11f24e23d62e05f85303d4.jpg")
                .searchVolume(11100000L)
                .category("Gaming")
                .build(),
            Item.builder()
                .title("Minecraft")
                .imageUrl("https://wallpapers.com/images/high/herobrine-flower-field-q4054nlko2h8tb5r.webp")
                .searchVolume(20400000L)
                .category("Gaming")
                .build(),
            Item.builder()
                .title("Pokemon")
                .imageUrl("https://i.pinimg.com/736x/50/c4/e2/50c4e2b8180a85080158f3e9288c6244.jpg")
                .searchVolume(22200000L)
                .category("Gaming")
                .build(),
            Item.builder()
                .title("PUBG")
                .imageUrl("https://i.pinimg.com/736x/fd/49/5a/fd495a081b0f33d93e6644096c0feb78.jpg")
                .searchVolume(11100000L)
                .category("Gaming")
                .build(),
            Item.builder()
                .title("Roblox")
                .imageUrl("https://i.pinimg.com/736x/05/42/85/0542857dafff900e972a73c33b4cd969.jpg")
                .searchVolume(18100000L)
                .category("Gaming")
                .build(),
            Item.builder()
                .title("Valorant")
                .imageUrl("https://i.pinimg.com/736x/1c/c2/d9/1cc2d9236dc159ffb2209c14c6e433a1.jpg")
                .searchVolume(9140000L)
                .category("Gaming")
                .build(),
            Item.builder()
                .title("Baby Yoda")
                .imageUrl("https://i.pinimg.com/736x/f4/5a/1d/f45a1db349e5dee9d35f59c7c4cad6f3.jpg")
                .searchVolume(2240000L)
                .category("Memes")
                .build(),
            Item.builder()
                .title("Distracted Boyfriend")
                .imageUrl("https://i.pinimg.com/1200x/46/67/1f/46671fbc353f47a0bbc022ddb27b0524.jpg")
                .searchVolume(450000L)
                .category("Memes")
                .build(),
            Item.builder()
                .title("Doge")
                .imageUrl("https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=800")
                .searchVolume(1500000L)
                .category("Memes")
                .build(),
            Item.builder()
                .title("Grumpy Cat")
                .imageUrl("https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=800")
                .searchVolume(673000L)
                .category("Memes")
                .build(),
            Item.builder()
                .title("Pepe the Frog")
                .imageUrl("https://i.pinimg.com/736x/e7/57/92/e75792c8c95246ee23c15976d2f5e053.jpg")
                .searchVolume(1000000L)
                .category("Memes")
                .build(),
            Item.builder()
                .title("Rickroll")
                .imageUrl("https://i.pinimg.com/1200x/25/52/d2/2552d245e96f33a0f53f488f6dbba114.jpg")
                .searchVolume(1830000L)
                .category("Memes")
                .build(),
            Item.builder()
                .title("SpongeBob")
                .imageUrl("https://i.pinimg.com/736x/02/89/ca/0289caf6dc68083315608522dffde40b.jpg")
                .searchVolume(4090000L)
                .category("Memes")
                .build(),
            Item.builder()
                .title("Avengers")
                .imageUrl("https://i.pinimg.com/1200x/91/e8/b2/91e8b28b4cb04f5bd07d7fcd3bf08e16.jpg")
                .searchVolume(11100000L)
                .category("Movies and TV")
                .build(),
            Item.builder()
                .title("Batman")
                .imageUrl("https://i.pinimg.com/736x/fc/49/fe/fc49fef80f27f04a24346ece533199e8.jpg")
                .searchVolume(9140000L)
                .category("Movies and TV")
                .build(),
            Item.builder()
                .title("Black Panther")
                .imageUrl("https://i.pinimg.com/1200x/6c/15/83/6c1583ecc6b9ff42bcf92f20853c9d42.jpg")
                .searchVolume(5000000L)
                .category("Movies and TV")
                .build(),
            Item.builder()
                .title("Fast and Furious")
                .imageUrl("https://i.pinimg.com/1200x/00/b4/69/00b469fc87bb4e85202f0631a14c9065.jpg")
                .searchVolume(6120000L)
                .category("Movies and TV")
                .build(),
            Item.builder()
                .title("Harry Potter")
                .imageUrl("https://cdn.britannica.com/81/152981-050-7891A7CF/Daniel-Radcliffe-Harry-Potter-and-the-Philosophers.jpg")
                .searchVolume(13600000L)
                .category("Movies and TV")
                .build(),
            Item.builder()
                .title("Jurassic Park")
                .imageUrl("https://i.pinimg.com/736x/a8/a7/70/a8a7702721817c8289afefe6a8db969f.jpg")
                .searchVolume(3350000L)
                .category("Movies and TV")
                .build(),
            Item.builder()
                .title("Spider-Man")
                .imageUrl("https://i.pinimg.com/1200x/0c/6f/e8/0c6fe80e89b3810f669c88940ad9e5a1.jpg")
                .searchVolume(13600000L)
                .category("Movies and TV")
                .build(),
            Item.builder()
                .title("Star Wars")
                .imageUrl("https://images.unsplash.com/photo-1472457897821-70d3819a0e24?q=80&w=1169")
                .searchVolume(9140000L)
                .category("Movies and TV")
                .build(),
            Item.builder()
                .title("The Lion King")
                .imageUrl("https://i.pinimg.com/1200x/fd/0c/44/fd0c44fd41b80385b1a21999a42195f9.jpg")
                .searchVolume(4090000L)
                .category("Movies and TV")
                .build(),
            Item.builder()
                .title("Titanic")
                .imageUrl("https://images.unsplash.com/photo-1505118380757-91f5f5632de0?w=800")
                .searchVolume(5000000L)
                .category("Movies and TV")
                .build(),
            Item.builder()
                .title("Cristiano Ronaldo")
                .imageUrl("https://c4.wallpaperflare.com/wallpaper/348/390/445/cristiano-ronaldo-kiev-ukraine-uefa-wallpaper-preview.jpg")
                .searchVolume(37200000L)
                .category("Sports")
                .build(),
            Item.builder()
                .title("Kylian Mbappe")
                .imageUrl("https://i.pinimg.com/736x/8c/78/fd/8c78fdeda42f75009ebdc924c559ff1a.jpg")
                .searchVolume(9140000L)
                .category("Sports")
                .build(),
            Item.builder()
                .title("LeBron James")
                .imageUrl("https://i.pinimg.com/1200x/be/30/b3/be30b3962006f548feeeb4c3d173c34e.jpg")
                .searchVolume(11100000L)
                .category("Sports")
                .build(),
            Item.builder()
                .title("Lionel Messi")
                .imageUrl("https://images.unsplash.com/photo-1667983090922-3a996b026a26?q=80&w=1170")
                .searchVolume(33100000L)
                .category("Sports")
                .build(),
            Item.builder()
                .title("Michael Jordan")
                .imageUrl("https://i.pinimg.com/736x/e3/d0/09/e3d0097284432d932d1fec93b0327129.jpg")
                .searchVolume(9140000L)
                .category("Sports")
                .build(),
            Item.builder()
                .title("MS Dhoni")
                .imageUrl("https://i.pinimg.com/736x/07/f2/12/07f2126251f52b43878734c453f1b4a2.jpg")
                .searchVolume(6120000L)
                .category("Sports")
                .build(),
            Item.builder()
                .title("NBA")
                .imageUrl("https://i.pinimg.com/736x/30/5e/51/305e516eb5884151b129e6d1def75f77.jpg")
                .searchVolume(40500000L)
                .category("Sports")
                .build(),
            Item.builder()
                .title("Neymar")
                .imageUrl("https://i.pinimg.com/736x/59/1a/40/591a40b22dbe7aba1ccf55c48f0627a4.jpg")
                .searchVolume(7480000L)
                .category("Sports")
                .build(),
            Item.builder()
                .title("Roger Federer")
                .imageUrl("https://img.olympics.com/images/image/private/t_s_16_9_g_auto/t_s_w1460/f_auto/primary/j4rznjbm2x7efqov4r7r")
                .searchVolume(4090000L)
                .category("Sports")
                .build(),
            Item.builder()
                .title("Ronaldinho")
                .imageUrl("https://i.pinimg.com/736x/ec/82/72/ec8272b3fbae89645109b291aea3dcfb.jpg")
                .searchVolume(3350000L)
                .category("Sports")
                .build(),
            Item.builder()
                .title("Serena Williams")
                .imageUrl("https://i.pinimg.com/1200x/0c/58/3c/0c583c5cf02a6b4bbc43e4fe03885b7d.jpg")
                .searchVolume(3350000L)
                .category("Sports")
                .build(),
            Item.builder()
                .title("Usain Bolt")
                .imageUrl("https://media.self.com/photos/57d88409f71ce8751f6b46cd/4:3/w_2560%2Cc_limit/usainbolt.jpg")
                .searchVolume(2240000L)
                .category("Sports")
                .build(),
            Item.builder()
                .title("Virat Kohli")
                .imageUrl("https://images.unsplash.com/photo-1565787154274-c8d076ad34e7?q=80&w=765")
                .searchVolume(9140000L)
                .category("Sports")
                .build(),
            Item.builder()
                .title("Amazon")
                .imageUrl("https://www.investopedia.com/thmb/-1ZX3x2sr3haLD4f85Do8n8ybhg=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/GettyImages-1248364818-d6f6d28199d740a4a1e87b6009099772.jpg")
                .searchVolume(206000000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("ChatGPT")
                .imageUrl("https://i.pinimg.com/1200x/39/5e/84/395e84ce15a9e724114b1c433111da83.jpg")
                .searchVolume(91500000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("Facebook")
                .imageUrl("https://i.pinimg.com/736x/7b/ed/39/7bed398644d61cae7c4dd853b558a1c9.jpg")
                .searchVolume(182000000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("Google")
                .imageUrl("https://i.pinimg.com/736x/d7/f9/f1/d7f9f1fb2c031e7721b273a90eaa11bf.jpg")
                .searchVolume(500000000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("Instagram")
                .imageUrl("https://images.unsplash.com/photo-1611162616305-c69b3fa7fbe0?q=80&w=1974")
                .searchVolume(165000000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("iPhone")
                .imageUrl("https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?w=800")
                .searchVolume(20400000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("LinkedIn")
                .imageUrl("https://i.pinimg.com/736x/6c/41/53/6c41531c905f8c6c4b4c984cc143e0cf.jpg")
                .searchVolume(135000000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("Netflix")
                .imageUrl("https://images.unsplash.com/photo-1643208589889-0735ad7218f0?q=80&w=1169")
                .searchVolume(110000000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("OpenAI")
                .imageUrl("https://i.pinimg.com/736x/e3/c3/f5/e3c3f59d4880c5834a8dabf21e983c00.jpg")
                .searchVolume(27100000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("PlayStation 5")
                .imageUrl("https://images.unsplash.com/photo-1606144042614-b2417e99c4e3?q=80&w=1170")
                .searchVolume(5000000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("Reddit")
                .imageUrl("https://i.pinimg.com/736x/72/b7/fc/72b7fc647fa82c4c54a474e115d2e9ae.jpg")
                .searchVolume(82000000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("Snapchat")
                .imageUrl("https://i.pinimg.com/736x/4a/b5/9b/4ab59bf6437538d0d99264293ef3c479.jpg")
                .searchVolume(45500000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("Spotify")
                .imageUrl("https://i.pinimg.com/1200x/16/38/1f/16381f7c2b21da49b1afb3529c46f41a.jpg")
                .searchVolume(99000000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("Tesla")
                .imageUrl("https://i.pinimg.com/1200x/99/63/39/9963395ed6dbc066c932893c14a16ece.jpg")
                .searchVolume(20400000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("TikTok")
                .imageUrl("https://images.unsplash.com/photo-1611605698323-b1e99cfd37ea?q=80&w=1074")
                .searchVolume(91500000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("Twitter")
                .imageUrl("https://www.lifewire.com/thmb/mQBVGczEYL-mea6s7goWTFzg3v0=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc():format(webp)/Twitter-and-X-4c4103f6bc3c42e0b7197b60a50317ca.jpg")
                .searchVolume(82000000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("Uber")
                .imageUrl("https://i.pinimg.com/736x/2c/ea/3e/2cea3e7494f8f6f763216b708c21f4f2.jpg")
                .searchVolume(22200000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("WhatsApp")
                .imageUrl("https://images.unsplash.com/photo-1636751364472-12bfad09b451?q=80&w=1170")
                .searchVolume(121000000L)
                .category("Technology")
                .build(),
            Item.builder()
                .title("YouTube")
                .imageUrl("https://images.unsplash.com/photo-1611162616475-46b635cb6868?q=80&w=1074")
                .searchVolume(165000000L)
                .category("Technology")
                .build()
        );
        itemRepository.saveAll(items);
        log.info("DataSeeder: {} items seeded successfully.", items.size());
    }
}

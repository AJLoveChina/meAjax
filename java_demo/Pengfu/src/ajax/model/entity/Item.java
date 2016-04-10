package ajax.model.entity;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import javax.imageio.ImageIO;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.json.JSONObject;
import org.json.JSONString;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ajax.model.JokeStatus;
import ajax.model.JokeType;
import ajax.model.QueryParams;
import ajax.model.UrlRoute;
import ajax.spider.Spider3;
import ajax.spider.rules.Rules;
import ajax.spider.rules.RulesTag;
import ajax.spider.rules.SpiderWeb;
import ajax.tools.HibernateUtil;
import ajax.tools.Tools;


public class Item extends Entity<Item> implements Iterable<Item>, JSONString{
	private int id;
	private String url;
	private String title;
	private String summary;
	private String content;
	private String stamps;
	private int likes;
	private int dislikes;
	private boolean hasGetImage = false;
	private int itype;
	private int status;
	private String username;
	private String userPersonalPageUrl;
	private String backgroundInformation;
	private String dateEntered;
	private int rulesTagId;
	private String previewImage;
	/**
	 * 属于哪一页
	 */
	private int page;
	
	
	private String[] $stampsArr;
	
	
	
	
	public String[] get$stampsArr() {
		return this.getStamps().split(",");
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public String getPreviewImage() {
		return previewImage;
	}
	public void setPreviewImage(String previewImage) {
		this.previewImage = previewImage;
	}
	public int getRulesTagId() {
		return rulesTagId;
	}
	public void setRulesTagId(int rulesTagId) {
		this.rulesTagId = rulesTagId;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getStamps() {
		return stamps;
	}
	public void setStamps(String stamps) {
		this.stamps = stamps;
	}
	public int getLikes() {
		return likes;
	}
	public void setLikes(int likes) {
		this.likes = likes;
	}
	public int getDislikes() {
		return dislikes;
	}
	public void setDislikes(int dislikes) {
		this.dislikes = dislikes;
	}
	public boolean isHasGetImage() {
		return hasGetImage;
	}
	public void setHasGetImage(boolean hasGetImage) {
		this.hasGetImage = hasGetImage;
	}
	public int getItype() {
		return itype;
	}
	public void setItype(int itype) {
		this.itype = itype;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUserPersonalPageUrl() {
		return userPersonalPageUrl;
	}
	public void setUserPersonalPageUrl(String userPersonalPageUrl) {
		this.userPersonalPageUrl = userPersonalPageUrl;
	}
	public String getBackgroundInformation() {
		return backgroundInformation;
	}
	public void setBackgroundInformation(String backgroundInformation) {
		this.backgroundInformation = backgroundInformation;
	}
	public String getDateEntered() {
		return dateEntered;
	}
	public void setDateEntered(String dateEntered) {
		this.dateEntered = dateEntered;
	}
	
	
	/**
	 * @return 实体的jokeType realname
	 */
	public String getITypeRealName() {
		JokeType jt = JokeType.getJokeType(this.itype);
		return jt.getRealName();
	}
	
	public void updateBySpider() {
		final String url = this.getUrl();
		final JokeType jokeType = JokeType.getJokeType(this.getItype());
		final RulesTag rulesTag = RulesTag.getRulesTagById(this.getRulesTagId());
		
		Spider3 sp3 = new Spider3() {
			
			@Override
			public SpiderWeb returnSpiderWeb() {
				return new SpiderWeb() {
					
					@Override
					public String returnUrl() {
						return url;
					}
					
					@Override
					public Rules returnRules() {
						try {
							
							return (Rules) Class.forName("ajax.spider.rules.ZhihuAnswerRules").newInstance();
							
						} catch (InstantiationException e) {
							System.out.println("Error : " + e.getMessage());
						} catch (IllegalAccessException e) {
							System.out.println("Error : " + e.getMessage());
						} catch (ClassNotFoundException e) {
							System.out.println("Error : " + e.getMessage());
						}
						return null;
					}
					
					@Override
					public JokeType returnJokeType() {
						return jokeType;
					}
				};
			}
		};
		
		sp3.update(this.getId());
	}
	
	
	public static Item getByItemById(int id) {
		
		Session session = HibernateUtil.getSession();
		
		Item entity = (Item)session.get(Item.class, id);
		
		return entity;
		
	}
	
	public String getOneJokeUrlById() {
		return UrlRoute.ONEJOKE + "?id=" + this.getId(); 
	}
	
	public boolean hasAuthor() {
		return (this.getUsername() != null && this.getUsername().trim() != "");
	}
	
	/**
	 * 是否有预览图片
	 * @return
	 */
	public boolean hasPreviewImage() {
		return this.previewImage != null;
	}
	
	/**
	 * 根据QueryParams 对象查询 item实体集
	 * @param qp
	 * @return
	 */
	public static List<Item> query(QueryParams qp) {
		
		Session session = HibernateUtil.getSession();
		
		Criteria criteria = session.createCriteria(Item.class);
		
		
		int page = 1;
		int size = 10;
		
		if (qp.isSet("page")) {
			page = Tools.parseInt(qp.getVal("page"), 1);
		}
		if (qp.isSet("size")) {
			size = Tools.parseInt(qp.getVal("size"), 10);
		}
			
		criteria.setFirstResult((page - 1) * size);
		criteria.setMaxResults(size);
		
		if (qp.isSet("type")) {
			criteria.add(Restrictions.eq("itype", Tools.parseInt(qp.getVal("type"), JokeType.UNKNOWN.getId())));
		}
		
		return criteria.list();
	}
	
	/**
	 * 根据content获取图片并保存到本地磁盘<br>
	 * return new Content that contains imgs which src is alright.
	 * 注意该方法不会在抓取完毕后更新 content 的值, 如果需要抓取后更新实体请使用 grabImagesFromContentAndUpdate
	 */
	public String grabImagesFromContent() {
		int rulesTagid = this.getRulesTagId();
		RulesTag rt = RulesTag.getRulesTagById(rulesTagid);
		String folder = rt.getImageFolder();
		
		String newContent;
		try {
			
			newContent = Tools.grabImagesFromString(new URL(this.getUrl()), this.getContent(), folder);
			
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			newContent = this.getContent();
		}
		
		return newContent;
	}
	
	/**
	 * 根据content获取图片并保存到本地磁盘<br>抓取后更新实体
	 */
	public void grabImagesFromContentAndUpdate() {
		
		String newContent = this.grabImagesFromContent();
		this.setContent(newContent);
		this.setHasGetImage(true);
		this.update();
		
	}
	
	/**
	 * 根据QueryParams 获取对应的URL
	 * @param qp
	 * @return
	 */
	public static String getHrefByQueryParams(UrlRoute urlRoute, QueryParams qp) {
		StringBuilder sb = new StringBuilder();
		sb.append(urlRoute.getUrl());
		sb.append("?");
		
		
		
		
		return sb.toString();
	}
	/**
	 * 根据content生成summary内容并update实体
	 */
	public void generateSummary() {
		Document doc = Jsoup.parse(this.getContent());
		
		try {
			
			String text = doc.body().text();
			
			String summary;
			int length,random;
			if (this.getPreviewImage() == null || this.getPreviewImage() == "") {
				length = 170;
				random = (new Random()).nextInt(40);
				
				summary = text.substring(0, length + random);
			} else {
				length = 110;
				random = (new Random()).nextInt(20);
				summary = text.substring(0, length + random);
			}
			
			this.setSummary(summary);
			
			this.update();
			
		}catch(Exception e) {
			String summary = doc.body().text();
			this.setSummary(summary);
			this.update();
		}
		
	}
	/**
	 * 根据content获取一张代表图片并update实体
	 */
	public void generateItemImage() {
		try {
			Document doc = Jsoup.parse(this.getContent());
			
			Elements imgs = doc.select("img");
			
			Map<String, Float> map = new HashMap<String, Float>();
			
			if (imgs.size() > 0) {
				for (Element img : imgs) {
					String src = img.attr("src");
					ImagesContainer ic = ImagesContainer.getByWebPath(src);
					
					if (ic != null) {
						try {
							File picture = new File(ic.getDiskPath());
							BufferedImage sourceImg =ImageIO.read(new FileInputStream(picture));
							if (sourceImg.getWidth() > 50) {
								map.put(ic.getWebPath(), Math.abs((float)sourceImg.getWidth() / sourceImg.getHeight() - 1));
							}
						}catch(Exception e) {
							System.out.println(e.getMessage());
						}
					}
					
				}
				
				String result = null;
				float des = 999;
				
				for(String key : map.keySet()) {
					if (map.get(key) < des) {
						result = key;
					}
				}
				
				this.setPreviewImage(result);
				this.update();
			}
			
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	@Override
	public Iterator<Item> iterator() {
		return new Iterator<Item>() {
			
			private int page = 1;
			private int size = 1;
			private boolean hasNext = true;
			
			@Override
			public Item next() {
				Session session = HibernateUtil.getSession();
				
				Criteria cr = session.createCriteria(Item.class);
				cr.setFirstResult((page - 1) * size);
				cr.setMaxResults(size);
				
				List<Item> items = cr.list();
				
				
				HibernateUtil.closeSession(session);
				
				if (items.size() > 0) {
					page ++;
					return items.get(0);
				} else {
					hasNext = false;
					return null;
				}
				
			}
			
			@Override
			public boolean hasNext() {
				return hasNext;
			}
		};
	}
	/**
	 * 返回oneItem page 的url地址
	 * @return
	 */
	public String getOneItemPageUrl() {
		return UrlRoute.ONEJOKE.getUrl() + "?id=" + this.id;
	}
	public static String getOneItemPageUrl(int id) {
		return UrlRoute.ONEJOKE.getUrl() + "?id=" + id;
	}
	
	/**
	 * 获取一个还没有放入page表的item(未放入page表的item的page字段值为0)
	 * @return
	 */
	public static Item getOneItemWhichIsNotInPage() {
		Session session = HibernateUtil.getSession();
		Criteria cr = session.createCriteria(Item.class);
		cr.add(Restrictions.eq("page", 0));
		cr.add(Restrictions.gt("likes", 500));
		cr.add(Restrictions.ne("status", JokeStatus.DELETE.getId()));
		cr.setMaxResults(200);
		
		List<Item> items = cr.list();
		Random rd = new Random();
		int rand = rd.nextInt(items.size());
		Item item = items.get(rand);
		
		HibernateUtil.closeSession(session);
		
		return item;
	}
	public static List<Item> get(List<Integer> itemsId) {
		Session session = HibernateUtil.getSession();
		Criteria cr = session.createCriteria(Item.class);
		
		cr.add(Restrictions.in("id", itemsId));
		List<Item> items = cr.list();
		HibernateUtil.closeSession(session);
		return items;
		
	}
	
	
	@Override
	public String toJSONString() {
		JSONObject jo = new JSONObject();
			
		jo.put("id", this.getId());
		jo.put("url", this.getUrl());
		jo.put("title", this.getTitle());
		jo.put("summary", this.getSummary());
		jo.put("content", this.getContent());
		jo.put("stamps", this.getStamps());
		jo.put("likes", this.getLikes());
		jo.put("dislikes", this.getDislikes());
		jo.put("hasGetImage", this.isHasGetImage());
		jo.put("itype", this.getItype());
		jo.put("status", this.getStatus());
		jo.put("username", this.getUsername());
		jo.put("userPersonalPageUrl", this.getUserPersonalPageUrl());
		jo.put("backgroundInformation", this.getBackgroundInformation());
		jo.put("dateEntered", this.getDateEntered());
		jo.put("rulesTagId", this.getRulesTagId());
		jo.put("previewImage", this.getPreviewImage());
		jo.put("page", this.getPage());
		
		return jo.toString();
	}
	
	/**
	 * 将item的content中的图片设置成延时加载的图片
	 */
	public void lazyImage() {
		Document doc = Jsoup.parse(this.getContent());
		
		Elements imgs = doc.select("img");
		
		for (Element ele : imgs) {
			if (!ele.hasClass("aj-lazy")) {
				ele.addClass("aj-lazy");
				ele.attr("data-lazy", ele.attr("src"));
				ele.removeAttr("src");
			}
			ele.attr("src", "web/pic/dot.jpg");
		
		}
		
		doc.select("noscript").remove();
		this.setContent(doc.body().html());
		this.update();
		
	}
	
	/**
	 * 把该item从对应的page 删除, 替换另一个随机的item. <br>
	 * 如果不想随机替代, 请添加参数 id
	 */
	public void removeFromPage() {
		Item item = Item.getOneItemWhichIsNotInPage();
		
		this.removeFromPage(item);
	}
	
	public void removeFromPage(int replaceid) {
		Item item = new Item();
		item.load(replaceid);
		
		if (item != null) {
			this.removeFromPage(item);
		}
	}
	
	private void removeFromPage(Item item) {
		Page page = Page.getByPage(this.getPage());
		
		List<Integer> itemsid = page.get$items();
		
		for (int i = 0; i < itemsid.size(); i++) {
			if (itemsid.get(i) == this.getId()) {
				itemsid.set(i, item.getId());
			}
		}
		
		page.set$items(itemsid);
		page.update(); // 更新 page
		
		
		item.setPage(this.getPage());
		item.update(); // 更新 要替换的item
		
		
		this.setPage(0);
		this.setStatus(JokeStatus.DELETE.getId());
		this.update(); // 更新 被替换的item
		
		System.out.println("已将 " + this.getId() + " 替换成  " + item.getId());
		
	}
	/**
	 * generate item の jokeType
	 */
	public void generateType() {
		String[] stamps = this.get$stampsArr();
		
		for (String stamp : stamps) {
		
			JokeType jokeType = JokeType.guessType(stamp.trim());
			
			if (jokeType != null) {
				this.setItype(jokeType.getId());
				this.update();
				System.out.println("Generate itype ok" + this.getTitle() + " type" + jokeType.getRealName());
				return;
			}
		}
		
		for (JokeType type : JokeType.getAllJokeTypes()) {
			String[] stampArr = type.getInfo().split(",");
			
			for (String s : stampArr) {
				if (this.getContent().contains(s)) {
					this.setItype(type.getId());
					this.update();
					System.out.println("Generate itype ok" + this.getTitle() + " type" + type.getRealName());
					return;
				}
			}
		}
		
		System.out.println("Generate itype fail" + this.getTitle());
	}
	
}



